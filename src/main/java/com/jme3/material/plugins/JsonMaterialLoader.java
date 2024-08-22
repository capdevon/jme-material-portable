package com.jme3.material.plugins;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetLoader;
import com.jme3.asset.AssetManager;
import com.jme3.asset.AssetNotFoundException;
import com.jme3.asset.JsonMaterialKey;
import com.jme3.asset.TextureKey;
import com.jme3.material.MatParam;
import com.jme3.material.Material;
import com.jme3.material.MaterialDef;
import com.jme3.material.RenderState;
import com.jme3.material.RenderState.BlendEquation;
import com.jme3.material.RenderState.BlendEquationAlpha;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.material.RenderState.TestFunction;
import com.jme3.material.TechniqueDef;
import com.jme3.material.TechniqueDef.LightMode;
import com.jme3.material.TechniqueDef.LightSpace;
import com.jme3.material.TechniqueDef.ShadowMode;
import com.jme3.material.logic.DefaultTechniqueDefLogic;
import com.jme3.material.logic.MultiPassLightingLogic;
import com.jme3.material.logic.SinglePassAndImageBasedLightingLogic;
import com.jme3.material.logic.SinglePassLightingLogic;
import com.jme3.material.logic.StaticPassLightingLogic;
import com.jme3.material.utils.StringUtils;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.shader.DefineList;
import com.jme3.shader.Shader.ShaderType;
import com.jme3.shader.VarType;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.MagFilter;
import com.jme3.texture.Texture.MinFilter;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.texture.Texture2D;
import com.jme3.texture.image.ColorSpace;
import com.jme3.util.PlaceholderAssets;
import com.jme3.util.clone.Cloner;

/**
 * 
 * @author capdevon
 */
public class JsonMaterialLoader implements AssetLoader {

    private static final Logger logger = Logger.getLogger(JsonMaterialLoader.class.getName());

    private AssetManager assetManager;
    private AssetKey<?> key;
    
    private Material material;
    private MaterialDef materialDef;
    private TechniqueDef technique;
    private final ArrayList<String> presetDefines = new ArrayList<>();

    private final List<EnumMap<ShaderType, String>> shaderLanguages;
    private final EnumMap<ShaderType, String> shaderNames;

    public JsonMaterialLoader() {
        shaderLanguages = new ArrayList<>();
        shaderNames = new EnumMap<>(ShaderType.class);
    }
    
    @Override
    public Object load(AssetInfo info) throws IOException {
        clearCache();
        
        this.assetManager = info.getManager();

        try (InputStream in = info.openStream()) {
            this.key = info.getKey();

            // Create a JsonReader from the InputStream
            JsonReader reader = new JsonReader(new InputStreamReader(in, StandardCharsets.UTF_8));

            // Parse JSON using Gson streaming API
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
            loadFromRoot(jsonObject);
        }

        if (material != null) {
            // material implementation
            return material;
        } else {
            // material definition
            return materialDef;
        }
    }

    /**
     */
    private void clearCache() {
        material = null;
        materialDef = null;
    }
    
    /**
     * Loads a material from a JSON resource file identified by an
     * AssetKey<JsonMaterialKey>. It parses the JSON content of the file and uses
     * the parsed data to create a material object.
     * 
     * @param assetManager An AssetManager instance used to access resources.
     * @param key          An AssetKey<JsonMaterialKey> representing the unique
     *                     identifier of the material resource.
     * @return A Material object representing the loaded material.
     */
    public Material loadMaterial(AssetManager assetManager, AssetKey<JsonMaterialKey> key) {
        this.assetManager = assetManager;
        this.key = key;

        try (FileReader reader = new FileReader(getResource(key.getName()))) {
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
            loadFromRoot(jsonObject);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // return the Material object
        return material;
    }
    
    private String getResource(String name) throws IOException {
        ClassLoader loader = getClass().getClassLoader();
        URL url = loader.getResource(name);
        if (url == null) {
            throw new IOException("Resource not found: " + name);
        }
        return url.getFile();
    }
    
    private void loadFromRoot(JsonObject jsonObject) throws IOException {
        
        // Get the top-level "Material" object
        JsonObject joMaterial = null;
        boolean extending = false;
        
        if (jsonObject.has("materialDef")) {
            joMaterial = jsonObject.getAsJsonObject("materialDef");
            extending = false;
            
        } else if (jsonObject.has("material")) {
            joMaterial = jsonObject.getAsJsonObject("material");
            extending = true;
            
        } else {
            throw new IOException("Specified file is not a Material file");
        }
        
        String materialName = joMaterial.get("name").getAsString();
        if (materialName.isBlank()) {
            throw new IOException("Material name cannot be empty: " + materialName);
        }
        
        if (!extending) {
            materialDef = new MaterialDef(assetManager, materialName);
            // NOTE: pass the filename for defs, so they can be loaded later
            materialDef.setAssetName(key.getName());
            
            // Parse MaterialParameters
            for (JsonElement el : joMaterial.getAsJsonArray("materialParameters")) {
                readParam(el.getAsJsonObject());
            }

            // Parse Techniques
            for (JsonElement el : joMaterial.getAsJsonArray("techniques")) {
                readTechnique(el.getAsJsonObject());
            }
        } else {
            // Extract properties
            String extendedMat = joMaterial.get("def").getAsString();
    
            MaterialDef def = assetManager.loadAsset(new AssetKey<MaterialDef>(extendedMat));
            if (def == null) {
                throw new IOException("Extended material " + extendedMat + " cannot be found.");
            }
    
            material = new Material(def);
            material.setKey(key);
            material.setName(materialName);
    
            // Parse MaterialParameters
            for (JsonElement el : joMaterial.getAsJsonArray("materialParameters")) {
                readValueParam(el.getAsJsonObject());
            }
    
            // Parse AdditionalRenderState
            JsonObject joRenderState = joMaterial.getAsJsonObject("additionalRenderState");
            readRenderState(material.getAdditionalRenderState(), joRenderState);
            
            // Parse Transparent
            if (jsonObject.has("transparent")) {
                boolean isTransparent = jsonObject.get("transparent").getAsBoolean();
                material.setTransparent(isTransparent);
            }
        }
    }

    private void readRenderState(RenderState renderState, JsonObject joRenderState) throws IOException {

        if (joRenderState.has("faceCull")) {
            String value = joRenderState.get("faceCull").getAsString();
            renderState.setFaceCullMode(FaceCullMode.valueOf(value));
        }
        if (joRenderState.has("blend")) {
            String value = joRenderState.get("blend").getAsString();
            renderState.setBlendMode(BlendMode.valueOf(value));
        }
        if (joRenderState.has("wireframe")) {
            boolean value = joRenderState.get("wireframe").getAsBoolean();
            renderState.setWireframe(value);
        }
        if (joRenderState.has("depthWrite")) {
            boolean value = joRenderState.get("depthWrite").getAsBoolean();
            renderState.setDepthWrite(value);
        }
        if (joRenderState.has("depthTest")) {
            boolean value = joRenderState.get("depthTest").getAsBoolean();
            renderState.setDepthTest(value);
        }
        if (joRenderState.has("colorWrite")) {
            boolean value = joRenderState.get("colorWrite").getAsBoolean();
            renderState.setColorWrite(value);
        }
        if (joRenderState.has("polyOffset")) {
            float[] value = parseFloatArray(joRenderState.get("polyOffset"), 2);
            float factor = value[0];
            float units = value[1];
            renderState.setPolyOffset(factor, units);
        }
        
        if (joRenderState.has("blendEquation")) {
            String value = joRenderState.get("blendEquation").getAsString();
            renderState.setBlendEquation(BlendEquation.valueOf(value));
        }
        if (joRenderState.has("blendEquationAlpha")) {
            String value = joRenderState.get("blendEquationAlpha").getAsString();
            renderState.setBlendEquationAlpha(BlendEquationAlpha.valueOf(value));
        }
        if (joRenderState.has("depthFunc")) {
            String value = joRenderState.get("depthFunc").getAsString();
            renderState.setDepthFunc(TestFunction.valueOf(value));
        }
        if (joRenderState.has("lineWidth")) {
            float value = joRenderState.get("lineWidth").getAsFloat();
            renderState.setLineWidth(value);
        }
    }

    private void readValueParam(JsonObject joParam) throws IOException {
        String name = joParam.get("name").getAsString();

        // parse value
        MatParam param = material.getMaterialDef().getMaterialParam(name);
        if (param == null) {
            throw new IOException("The material parameter: " + name + " is undefined.");
        }

        VarType type = param.getVarType();
        Object valueObj = readValue(type, joParam);

        if (type.isTextureType()) {
            material.setTextureParam(name, type, (Texture) valueObj);
        } else {
            material.setParam(name, type, valueObj);
        }
    }

    private Object readValue(VarType type, JsonObject jsonObject) throws IOException {
        if (type.isTextureType()) {
            return parseTextureType(type, jsonObject.getAsJsonObject("texture"));
        } else {
            JsonElement value = jsonObject.get("value");
            switch (type) {
                case Int:
                    return value.getAsInt();
                case Float:
                    return value.getAsFloat();
                case Boolean:
                    return value.getAsBoolean();
                case Vector2:
                    float[] vec2 = parseFloatArray(value, 2);
                    return new Vector2f(vec2[0], vec2[1]);
                case Vector3:
                    float[] vec3 = parseFloatArray(value, 3);
                    return new Vector3f(vec3[0], vec3[1], vec3[2]);
                case Vector4:
                    float[] vec4 = parseFloatArray(value, 4);
                    return new ColorRGBA(vec4[0], vec4[1], vec4[2], vec4[3]); //TODO: Vector4f ?
                default:
                    throw new UnsupportedOperationException("Unknown type: " + type);
            }
        }
    }

    private float[] parseFloatArray(JsonElement el, int size) throws IOException {
        if (el.isJsonArray()) {
            JsonArray jsonArray = el.getAsJsonArray();
            if (jsonArray.size() == size) {
                float[] a = new float[size];
                for (int i = 0; i < a.length; i++) {
                    a[i] = jsonArray.get(i).getAsFloat();
                }
                return a;
            } else {
                throw new IOException("Invalid format, vector must have " + size + " entries");
            }
        } else {
            throw new IOException("JsonElement should be an array");
        }
    }

    private Texture parseTextureType(final VarType type, final JsonObject jsonObject) {

        TextureKey textureKey = null;
        String texturePath = jsonObject.get("path").getAsString();
        boolean flipY = false;

        if (jsonObject.has("flipY")) {
            flipY = jsonObject.get("flipY").getAsBoolean();
        }

        textureKey = new TextureKey(texturePath, flipY);
        textureKey.setGenerateMips(true);

        // Apply texture options to the textureKey
        if (jsonObject.has("minFilter")) {
            MinFilter min = MinFilter.valueOf(jsonObject.get("minFilter").getAsString());
            textureKey.setGenerateMips(min.usesMipMapLevels());
        }
        
        switch (type) {
            case Texture3D:
                textureKey.setTextureTypeHint(Texture.Type.ThreeDimensional);
                break;
            case TextureArray:
                textureKey.setTextureTypeHint(Texture.Type.TwoDimensionalArray);
                break;
            case TextureCubeMap:
                textureKey.setTextureTypeHint(Texture.Type.CubeMap);
                break;
            default:
                break;
        }

        Texture texture = null;
        try {
            texture = assetManager.loadTexture(textureKey);

        } catch (AssetNotFoundException ex) {
            logger.log(Level.WARNING, "Cannot locate {0} for material {1}", new Object[] { textureKey, key });
        }
        
        if (texture == null) {
            texture = new Texture2D(PlaceholderAssets.getPlaceholderImage(assetManager));
            texture.setKey(textureKey);
            texture.setName(textureKey.getName());
        }

        // Apply texture options to the texture
        if (jsonObject.has("wrapMode")) {
            String wrapMode = jsonObject.get("wrapMode").getAsString();
            texture.setWrap(WrapMode.valueOf(wrapMode));
        } else {
            Texture.WrapAxis[] axis = { Texture.WrapAxis.S, Texture.WrapAxis.T, Texture.WrapAxis.R };
            for (int i = 0; i < axis.length; i++) {
                String axisName = "wrap" + axis[i].name();
                if (jsonObject.has(axisName)) {
                    String wrapMode = jsonObject.get(axisName).getAsString();
                    texture.setWrap(axis[i], WrapMode.valueOf(wrapMode));
                }
            }
        }
        
        if (jsonObject.has("minFilter")) {
            String min = jsonObject.get("minFilter").getAsString();
            texture.setMinFilter(MinFilter.valueOf(min));
        }
        if (jsonObject.has("magFilter")) {
            String mag = jsonObject.get("magFilter").getAsString();
            texture.setMagFilter(MagFilter.valueOf(mag));
        }

        return texture;
    }

    // <TYPE> <NAME> [-LINEAR] [ ":" <DEFAULTVAL> ]
    private void readParam(JsonObject jsonObject) throws IOException {
        String name = jsonObject.get("name").getAsString();
        String stringType = jsonObject.get("type").getAsString();

        ColorSpace colorSpace = null;
        if (jsonObject.has("colorSpace")) {
            colorSpace = ColorSpace.valueOf(jsonObject.get("colorSpace").getAsString());
        }

        VarType type;
        if (stringType.equals("Color")) {
            type = VarType.Vector4;
        } else {
            type = VarType.valueOf(stringType);
        }

        Object defaultVal = null;
        if (jsonObject.has("value")) {
            defaultVal = readValue(type, jsonObject);
        }
        if (type.isTextureType()) {
            materialDef.addMaterialParamTexture(type, name, colorSpace, (Texture) defaultVal);
        } else {
            materialDef.addMaterialParam(type, name, defaultVal);
        }
    }
    
    private void readTechnique(JsonObject jsonObject) throws IOException {

        String name = jsonObject.get("name").getAsString();

        String techniqueUniqueName = materialDef.getAssetName() + "@" + name;
        technique = new TechniqueDef(name, techniqueUniqueName.hashCode());
        readTechniqueStatement(jsonObject);

        technique.setShaderPrologue(createShaderPrologue(presetDefines));

        switch (technique.getLightMode()) {
            case Disable:
                technique.setLogic(new DefaultTechniqueDefLogic(technique));
                break;
            case MultiPass:
                technique.setLogic(new MultiPassLightingLogic(technique));
                break;
            case SinglePass:
                technique.setLogic(new SinglePassLightingLogic(technique));
                break;
            case StaticPass:
                technique.setLogic(new StaticPassLightingLogic(technique));
                break;
            case SinglePassAndImageBased:
                technique.setLogic(new SinglePassAndImageBasedLightingLogic(technique));
                break;
            default:
                throw new IOException("Light mode not supported:" + technique.getLightMode());
        }

        List<TechniqueDef> techniqueDefs = new ArrayList<>();

        if (shaderNames.containsKey(ShaderType.Vertex) 
                && shaderNames.containsKey(ShaderType.Fragment)) {
            
            if (shaderLanguages.size() > 1) {
                Cloner cloner = new Cloner();
                for (int i = 1; i < shaderLanguages.size(); i++) {
                    cloner.clearIndex();
                    TechniqueDef td = cloner.clone(technique);
                    td.setShaderFile(shaderNames, shaderLanguages.get(i));
                    techniqueDefs.add(td);
                }
            }
            
            technique.setShaderFile(shaderNames, shaderLanguages.get(0));
            techniqueDefs.add(technique);

        } else {
            technique = null;
            shaderLanguages.clear();
            shaderNames.clear();
            presetDefines.clear();
            logger.log(Level.WARNING, "Fixed function technique ''{0}'' was ignored for material {1}",
                    new Object[] { name, key });
            return;
        }

        for (TechniqueDef techniqueDef : techniqueDefs) {
            materialDef.addTechniqueDef(techniqueDef);
        }

        technique = null;
        shaderLanguages.clear();
        shaderNames.clear();
        presetDefines.clear();
    }
    
    private String createShaderPrologue(List<String> presetDefines) {
        DefineList defList = new DefineList(presetDefines.size());
        for (int i = 0; i < presetDefines.size(); i++) {
            defList.set(i, 1);
        }
        StringBuilder sb = new StringBuilder();
        defList.generateSource(sb, presetDefines, null);
        return sb.toString();
    }
    
    private void readTechniqueStatement(JsonObject jsonObject) throws IOException {
        /**
         * VertexShader
         * FragmentShader
         * GeometryShader
         * TessellationControlShader
         * TessellationEvaluationShader
         */
        readShaderStatement(jsonObject);

        if (jsonObject.has("lightMode")) {
            String lightMode = jsonObject.get("lightMode").getAsString();
            technique.setLightMode(LightMode.valueOf(lightMode));
        }
        if (jsonObject.has("lightSpace")) {
            String lightSpace = jsonObject.get("lightSpace").getAsString();
            technique.setLightSpace(LightSpace.valueOf(lightSpace));
        }
        if (jsonObject.has("shadowMode")) {
            String shadowMode = jsonObject.get("shadowMode").getAsString();
            technique.setShadowMode(ShadowMode.valueOf(shadowMode));
        }

        for (JsonElement el : jsonObject.getAsJsonArray("worldParameters")) {
            technique.addWorldParam(el.getAsString());
        }

        if (jsonObject.has("renderState")) {
            RenderState renderState = new RenderState();
            readRenderState(renderState, jsonObject.getAsJsonObject("renderState"));
            technique.setRenderState(renderState);
        }
        if (jsonObject.has("forcedRenderState")) {
            RenderState renderState = new RenderState();
            readRenderState(renderState, jsonObject.getAsJsonObject("forcedRenderState"));
            technique.setForcedRenderState(renderState);
        }
        if (jsonObject.has("defines")) {
            for (JsonElement el : jsonObject.getAsJsonArray("defines")) {
                readDefine(el.getAsJsonObject());
            }
        }
        if (jsonObject.has("noRender")) {
            technique.setNoRender(jsonObject.get("noRender").getAsBoolean());
        }
    }
    
    // <DEFINENAME> [ ":" <PARAMNAME> ]
    private void readDefine(JsonObject jsonObject) {
        
        String defineName = jsonObject.get("name").getAsString();
        String paramName = jsonObject.get("param").getAsString();
        
        MatParam param = materialDef.getMaterialParam(paramName);
        if (param == null) {
            logger.log(Level.WARNING, "In technique ''{0}'':\n" 
                    + "Define ''{1}'' mapped to non-existent material parameter ''{2}'', ignoring.", 
                    new Object[] { technique.getName(), defineName, paramName });
            return;
        }
        //presetDefines.add(defineName);

        VarType paramType = param.getVarType();
        technique.addShaderParamDefine(paramName, paramType, defineName);
    }
    
    // <TYPE> <LANG> : <SOURCE>
    private void readShaderStatement(JsonObject jsonObject) throws IOException {
        for (ShaderType shaderType : ShaderType.values()) {
            String shaderName = StringUtils.uncapitalize(shaderType.name()) + "Shader";
            if (jsonObject.has(shaderName)) {
                String path = jsonObject.get(shaderName).getAsString();
                String[] languages = parseStringArray(jsonObject.get("shaderLanguages"));
                readShaderDefinition(shaderType, path, languages);
            }
        }
    }
    
    private void readShaderDefinition(ShaderType shaderType, String name, String... languages) {
        shaderNames.put(shaderType, name);

        for (int i = 0; i < languages.length; i++) {
            if (i >= shaderLanguages.size()) {
                EnumMap<ShaderType, String> map = new EnumMap<>(ShaderType.class);
                shaderLanguages.add(map);
            }
            shaderLanguages.get(i).put(shaderType, languages[i]);
        }
    }
    
    private String[] parseStringArray(JsonElement el) throws IOException {
        if (el.isJsonArray()) {
            JsonArray jsonArray = el.getAsJsonArray();
            String[] a = new String[jsonArray.size()];
            for (int i = 0; i < a.length; i++) {
                a[i] = jsonArray.get(i).getAsString();
            }
            return a;
        } else {
            throw new IOException("JsonElement should be an array");
        }
    }
    
}
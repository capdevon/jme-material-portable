package com.jme3.material.plugins;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
     * 
     * @param assetManager
     * @param fileName
     * @return
     * @throws IOException
     */
//    public Material loadMaterial(AssetManager assetManager, String fileName) throws IOException {
//        
//        this.assetManager = assetManager;
//
//        try (FileReader reader = new FileReader(fileName)) {
//
//            Gson gson = new Gson();
//            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
//            loadFromRoot(jsonObject);
//
//            // return the Material object
//            return material;
//        }
//    }
    
//    public MaterialDef loadMaterialDef(AssetManager manager, AssetKey key) throws IOException {
//        this.key = key;
//        this.assetManager = manager;
//        loadFromRoot(roots);
//        return materialDef;
//    }

    private void loadFromRoot(JsonObject jsonObject) throws IOException {
        
        // Get the top-level "Material" object
        JsonObject joMaterial = null;
        boolean extending = false;
        
        if (jsonObject.has("MaterialDef")) {
            joMaterial = jsonObject.getAsJsonObject("MaterialDef");
            extending = false;
            
        } else if (jsonObject.has("Material")) {
            joMaterial = jsonObject.getAsJsonObject("Material");
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
            for (JsonElement el : joMaterial.getAsJsonArray("MaterialParameters")) {
                readParam(el.getAsJsonObject());
            }

            // Parse Techniques
            for (JsonElement el : joMaterial.getAsJsonArray("Techniques")) {
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

        String[] statements = { "faceCull", "blend", "wireframe", "depthWrite", "depthTest", "colorWrite", "polyOffset",
                "blendEquation", "blendEquationAlpha", "depthFunc", "lineWidth" };

        for (String str : statements) {
            if (!joRenderState.has(str)) {
                continue;
            }

            if (str.equals("faceCull")) {
                String value = joRenderState.get(str).getAsString();
                renderState.setFaceCullMode(FaceCullMode.valueOf(value));
            } else if (str.equals("blend")) {
                String value = joRenderState.get(str).getAsString();
                renderState.setBlendMode(BlendMode.valueOf(value));
            } else if (str.equals("wireframe")) {
                boolean value = joRenderState.get(str).getAsBoolean();
                renderState.setWireframe(value);
            } else if (str.equals("depthWrite")) {
                boolean value = joRenderState.get(str).getAsBoolean();
                renderState.setDepthWrite(value);
            } else if (str.equals("depthTest")) {
                boolean value = joRenderState.get(str).getAsBoolean();
                renderState.setDepthTest(value);
            } else if (str.equals("colorWrite")) {
                boolean value = joRenderState.get(str).getAsBoolean();
                renderState.setColorWrite(value);
            } else if (str.equals("polyOffset")) {
                float[] value = parseFloatArray(joRenderState.get(str), 2);
                float factor = value[0];
                float units = value[1];
                renderState.setPolyOffset(factor, units);
            }
            else if (str.equals("blendEquation")) {
                String value = joRenderState.get(str).getAsString();
                renderState.setBlendEquation(BlendEquation.valueOf(value));
            } else if (str.equals("blendEquationAlpha")) {
                String value = joRenderState.get(str).getAsString();
                renderState.setBlendEquationAlpha(BlendEquationAlpha.valueOf(value));
            } else if (str.equals("depthFunc")) {
                String value = joRenderState.get(str).getAsString();
                renderState.setDepthFunc(TestFunction.valueOf(value));
            } else if (str.equals("lineWidth")) {
                float value = joRenderState.get(str).getAsFloat();
                renderState.setLineWidth(value);
            }
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
            String wrap = jsonObject.get("wrapMode").getAsString();
            texture.setWrap(WrapMode.valueOf(wrap));
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

        if (shaderNames.containsKey(ShaderType.Vertex) && shaderNames.containsKey(ShaderType.Fragment)) {
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

        if (jsonObject.has("LightMode")) {
            String lightMode = jsonObject.get("LightMode").getAsString();
            technique.setLightMode(LightMode.valueOf(lightMode));
        }
        if (jsonObject.has("LightSpace")) {
            String lightSpace = jsonObject.get("LightSpace").getAsString();
            technique.setLightSpace(LightSpace.valueOf(lightSpace));
        }
        if (jsonObject.has("ShadowMode")) {
            String shadowMode = jsonObject.get("ShadowMode").getAsString();
            technique.setShadowMode(ShadowMode.valueOf(shadowMode));
        }

        for (JsonElement el : jsonObject.getAsJsonArray("WorldParameters")) {
            technique.addWorldParam(el.getAsString());
        }

        if (jsonObject.has("RenderState")) {
            RenderState renderState = new RenderState();
            readRenderState(renderState, jsonObject.getAsJsonObject("RenderState"));
            technique.setRenderState(renderState);
        }
        if (jsonObject.has("ForcedRenderState")) {
            RenderState renderState = new RenderState();
            readRenderState(renderState, jsonObject.getAsJsonObject("ForcedRenderState"));
            technique.setForcedRenderState(renderState);
        }
        if (jsonObject.has("Defines")) {
            for (JsonElement el : jsonObject.getAsJsonArray("Defines")) {
                readDefine(el.getAsJsonObject());
            }
        }
        if (jsonObject.has("NoRender")) {
            technique.setNoRender(jsonObject.get("NoRender").getAsBoolean());
        }
    }
    
    // <DEFINENAME> [ ":" <PARAMNAME> ]
    private void readDefine(JsonObject jsonObject) {
        String defineName = jsonObject.get("name").getAsString();
        presetDefines.add(defineName);

        String paramName = jsonObject.get("param").getAsString();
        MatParam param = materialDef.getMaterialParam(paramName);
        if (param == null) {
            logger.log(Level.WARNING, "In technique ''{0}'':\n" 
                    + "Define ''{1}'' mapped to non-existent material parameter ''{2}'', ignoring.", 
                    new Object[] { technique.getName(), defineName, paramName });
            return;
        }

        VarType paramType = param.getVarType();
        technique.addShaderParamDefine(paramName, paramType, defineName);
    }
    
    // <TYPE> <LANG> : <SOURCE>
    private void readShaderStatement(JsonObject jsonObject) throws IOException {
        for (ShaderType shaderType : ShaderType.values()) {
            String shaderName = shaderType.name() + "Shader";
            if (jsonObject.has(shaderName)) {
                String path = jsonObject.get(shaderName).getAsString();
                String[] languages = parseStringArray(jsonObject.get("ShaderLanguages"));
                readShaderDefinition(shaderType, path, languages);
            }
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
    
}
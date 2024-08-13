package com.jme3.material.plugins;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.reader.UnicodeReader;

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
public class YamlMaterialLoader implements AssetLoader {
    
    private static final Logger logger = Logger.getLogger(YamlMaterialLoader.class.getName());
    
    private AssetManager assetManager;
    private AssetKey<?> key;
    
    private Material material;
    private MaterialDef materialDef;
    private TechniqueDef technique;
    private final ArrayList<String> presetDefines = new ArrayList<>();

    private final List<EnumMap<ShaderType, String>> shaderLanguages;
    private final EnumMap<ShaderType, String> shaderNames;
    
    public YamlMaterialLoader() {
        shaderLanguages = new ArrayList<>();
        shaderNames = new EnumMap<>(ShaderType.class);
    }
    
    @Override
    public Object load(AssetInfo info) throws IOException {
        clearCache();
        this.assetManager = info.getManager();
        
        try (InputStream in = info.openStream()) {
            this.key = info.getKey();
            
            Yaml yaml = new Yaml();
            Map<String, Object> doc = yaml.load(new UnicodeReader(in));
            System.out.println(doc);
            loadFromRoot(doc);
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
    
    public Material loadMaterial(AssetManager assetManager, String fileName) {
        this.assetManager = assetManager;
        this.key = null;

        try (InputStream in = getResourceAsStream(fileName)) {
            Yaml yaml = new Yaml();
            Map<String, Object> doc = yaml.load(new UnicodeReader(in));
            System.out.println(doc);
            loadFromRoot(doc);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return material;
    }

    /**
     * Loads a resource file as an InputStream.
     * 
     * @throws IOException
     */
    private InputStream getResourceAsStream(String name) throws IOException {
        ClassLoader loader = getClass().getClassLoader();
        InputStream stream = loader.getResourceAsStream(name);
        if (stream == null) {
            throw new IOException("Resource not found: " + name);
        }
        return stream;
    }
    
    /**
     * @param doc
     * @throws IOException
     */
    private void loadFromRoot(Map<String, Object> doc) throws IOException {
        
        // Get the top-level "Material" object
        Map<String, Object> map = null;
        boolean extending = false;

        if (doc.containsKey("MaterialDef")) {
            map = (Map) doc.get("MaterialDef");
            extending = false;

        } else if (doc.containsKey("Material")) {
            map = (Map) doc.get("Material");
            extending = true;

        } else {
            throw new IOException("Specified file is not a Material file");
        }

        String materialName = getString(map.get("name"));
        if (materialName.isBlank()) {
            throw new IOException("Material name cannot be empty: " + materialName);
        }

        if (!extending) {
            materialDef = new MaterialDef(assetManager, materialName);
            // NOTE: pass the filename for defs, so they can be loaded later
            materialDef.setAssetName(key.getName());

            // Parse MaterialParameters
            List<Object> materialParameters = (List<Object>) map.get("MaterialParameters");
            for (Object el : materialParameters) {
                readParam((Map) el);
            }

            // Parse Techniques
            List<Object> techniques = (List<Object>) map.get("Techniques");
            for (Object el : techniques) {
                readTechnique((Map) el);
            }
        } else {
            // Extract properties
            String extendedMat = getString(map.get("def"));
    
            MaterialDef def = assetManager.loadAsset(new AssetKey<MaterialDef>(extendedMat));
            if (def == null) {
                throw new IOException("Extended material " + extendedMat + " cannot be found.");
            }
    
            material = new Material(def);
            material.setKey(key);   
            material.setName(materialName);
    
            // Parse MaterialParameters
            List<Object> materialParameters = (List) map.get("materialParameters");
            for (Object el : materialParameters) {
                readValueParam((Map) el);
            }
    
            // Parse AdditionalRenderState
            Map<String, Object> additionalRenderState = (Map) map.get("additionalRenderState");
            readRenderState(material.getAdditionalRenderState(), additionalRenderState);
        }
    }
    
    private void readRenderState(RenderState renderState, Map<String, Object> map) throws IOException {

        String[] statements = { "faceCull", "blend", "wireframe", "depthWrite", "depthTest", "colorWrite", "polyOffset",
                "blendEquation", "blendEquationAlpha", "depthFunc", "lineWidth" };

        for (String str : statements) {
            if (!map.containsKey(str)) {
                continue;
            }

            if (str.equals("faceCull")) {
                String value = getString(map.get(str));
                renderState.setFaceCullMode(FaceCullMode.valueOf(value));
            } else if (str.equals("blend")) {
                String value = getString(map.get(str));
                renderState.setBlendMode(BlendMode.valueOf(value));
            } else if (str.equals("wireframe")) {
                boolean value = getBoolean(map.get(str));
                renderState.setWireframe(value);
            } else if (str.equals("depthWrite")) {
                boolean value = getBoolean(map.get(str));
                renderState.setDepthWrite(value);
            } else if (str.equals("depthTest")) {
                boolean value = getBoolean(map.get(str));
                renderState.setDepthTest(value);
            } else if (str.equals("colorWrite")) {
                boolean value = getBoolean(map.get(str));
                renderState.setColorWrite(value);
            } else if (str.equals("polyOffset")) {
                float[] value = parseFloatArray(map.get(str), 2);
                float factor = value[0];
                float units = value[1];
                renderState.setPolyOffset(factor, units);
            }
            else if (str.equals("blendEquation")) {
                String value = getString(map.get(str));
                renderState.setBlendEquation(BlendEquation.valueOf(value));
            } else if (str.equals("blendEquationAlpha")) {
                String value = getString(map.get(str));
                renderState.setBlendEquationAlpha(BlendEquationAlpha.valueOf(value));
            } else if (str.equals("depthFunc")) {
                String value = getString(map.get(str));
                renderState.setDepthFunc(TestFunction.valueOf(value));
            } else if (str.equals("lineWidth")) {
                float value = getFloat(map.get(str));
                renderState.setLineWidth(value);
            }
        }
    }
    
    private void readValueParam(Map<String, Object> map) throws IOException {
        String name = getString(map.get("name"));

        // parse value
        MatParam param = material.getMaterialDef().getMaterialParam(name);
        if (param == null) {
            throw new IOException("The material parameter: " + name + " is undefined.");
        }

        VarType type = param.getVarType();
        Object valueObj = readValue(type, map);

        if (type.isTextureType()) {
            material.setTextureParam(name, type, (Texture) valueObj);
        } else {
            material.setParam(name, type, valueObj);
        }
    }

    private Object readValue(VarType type, Map<String, Object> map) throws IOException {
        if (type.isTextureType()) {
            return parseTextureType(type, (Map) map.get("texture"));
        } else {
            Object value = map.get("value");
            switch (type) {
                case Int:
                    return getInt(value);
                case Float:
                    return getFloat(value);
                case Boolean:
                    return getBoolean(value);
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
    
    private Texture parseTextureType(final VarType type, final Map<String, Object> map) throws IOException {

        TextureKey textureKey = null;
        String texturePath = getString(map.get("path"));
        boolean flipY = false;

        if (map.containsKey("flipY")) {
            flipY = getBoolean(map.get("flipY"));
        }

        textureKey = new TextureKey(texturePath, flipY);
        textureKey.setGenerateMips(true);

        // Apply texture options to the textureKey
        if (map.containsKey("minFilter")) {
            MinFilter min = getEnum(map.get("minFilter"), MinFilter.class);
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
        if (map.containsKey("wrapMode")) {
            WrapMode wrap = getEnum(map.get("wrapMode"), WrapMode.class);
            texture.setWrap(wrap);
        }
        if (map.containsKey("minFilter")) {
            MinFilter min = getEnum(map.get("minFilter"), MinFilter.class);
            texture.setMinFilter(min);
        }
        if (map.containsKey("magFilter")) {
            MagFilter mag = getEnum(map.get("magFilter"), MagFilter.class);
            texture.setMagFilter(mag);
        }

        return texture;
    }
    
    // <TYPE> <NAME> [-LINEAR] [ ":" <DEFAULTVAL> ]
    private void readParam(Map<String, Object> map) throws IOException {
        String name = getString(map.get("name"));
        String stringType = getString(map.get("type"));

        ColorSpace colorSpace = null;
        if (map.containsKey("colorSpace")) {
            colorSpace = getEnum(map.get("colorSpace"), ColorSpace.class);
        }

        VarType type;
        if (stringType.equals("Color")) {
            type = VarType.Vector4;
        } else {
            type = VarType.valueOf(stringType);
        }

        Object defaultVal = null;
        if (map.containsKey("value")) {
            defaultVal = readValue(type, map);
        }
        if (type.isTextureType()) {
            materialDef.addMaterialParamTexture(type, name, colorSpace, (Texture) defaultVal);
        } else {
            materialDef.addMaterialParam(type, name, defaultVal);
        }
    }
    
    private void readTechnique(Map<String, Object> map) throws IOException {

        String name = getString(map.get("name"));

        String techniqueUniqueName = materialDef.getAssetName() + "@" + name;
        technique = new TechniqueDef(name, techniqueUniqueName.hashCode());
        readTechniqueStatement(map);

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
    
    private void readTechniqueStatement(Map<String, Object> map) throws IOException {
        /**
         * VertexShader
         * FragmentShader
         * GeometryShader
         * TessellationControlShader
         * TessellationEvaluationShader
         */
        readShaderStatement(map);

        if (map.containsKey("LightMode")) {
            String lightMode = getString(map.get("LightMode"));
            technique.setLightMode(LightMode.valueOf(lightMode));
        }
        if (map.containsKey("LightSpace")) {
            String lightSpace = getString(map.get("LightSpace"));
            technique.setLightSpace(LightSpace.valueOf(lightSpace));
        }
        if (map.containsKey("ShadowMode")) {
            String shadowMode = getString(map.get("ShadowMode"));
            technique.setShadowMode(ShadowMode.valueOf(shadowMode));
        }

        List<Object> worldParameters = (List<Object>) map.get("WorldParameters");
        for (Object el : worldParameters) {
            technique.addWorldParam(el.toString());
        }

        if (map.containsKey("RenderState")) {
            RenderState renderState = new RenderState();
            readRenderState(renderState, (Map) map.get("RenderState"));
            technique.setRenderState(renderState);
        }
        if (map.containsKey("ForcedRenderState")) {
            RenderState renderState = new RenderState();
            readRenderState(renderState, (Map) map.get("ForcedRenderState"));
            technique.setForcedRenderState(renderState);
        }
        if (map.containsKey("Defines")) {
//            List<Object> defines = (List<Object>) map.get("Defines");
//            for (Object el : defines) {
//                readDefine((Map) el);
//            }
            
            readDefine((Map) map.get("Defines"));
        }
        if (map.containsKey("NoRender")) {
            boolean noRender = getBoolean(map.get("NoRender"));
            technique.setNoRender(noRender);
        }
    }
    
    // <DEFINENAME> [ ":" <PARAMNAME> ]
    private void readDefine(Map<String, Object> map) {
        
        for (Map.Entry<String, Object> entry : map.entrySet()) {

            String paramName = entry.getKey();
            String defineName = entry.getValue().toString();
            
            MatParam param = materialDef.getMaterialParam(paramName);
            if (param == null) {
                logger.log(Level.WARNING, "In technique ''{0}'':\n" 
                        + "Define ''{1}'' mapped to non-existent material parameter ''{2}'', ignoring.", 
                        new Object[] { technique.getName(), defineName, paramName });
            } else {
                presetDefines.add(defineName);

                VarType paramType = param.getVarType();
                technique.addShaderParamDefine(paramName, paramType, defineName);
            }
        }
        
//        String defineName = getString(map.get("name"));
//        presetDefines.add(defineName);
//
//        String paramName = getString(map.get("param"));
//        MatParam param = materialDef.getMaterialParam(paramName);
//        if (param == null) {
//            logger.log(Level.WARNING, "In technique ''{0}'':\n" 
//                    + "Define ''{1}'' mapped to non-existent material parameter ''{2}'', ignoring.", 
//                    new Object[] { technique.getName(), defineName, paramName });
//            return;
//        }
//
//        VarType paramType = param.getVarType();
//        technique.addShaderParamDefine(paramName, paramType, defineName);
    }
    
    // <TYPE> <LANG> : <SOURCE>
    private void readShaderStatement(Map<String, Object> map) throws IOException {
        for (ShaderType shaderType : ShaderType.values()) {
            String shaderName = shaderType.name() + "Shader";
            if (map.containsKey(shaderName)) {
                String path = getString(map.get(shaderName));
                String[] languages = parseStringArray(map.get("ShaderLanguages"));
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
    
    @SuppressWarnings("unchecked")
    protected static String[] parseStringArray(Object obj) throws IOException {
        if (obj instanceof List<?>) {
            List<Object> list = (List<Object>) obj;
            String[] a = new String[list.size()];
            for (int i = 0; i < a.length; i++) {
                a[i] = list.get(i).toString();
            }
            return a;
        } else {
            throw new IOException("Could not parse MaterialDef. Expected List at: " + obj);
        }
    }
    
    /// YAML NODE CONVERTERS

    protected static final List<String> YAML_YES_VALUES = Arrays.asList("y", "yes", "true", "on");
    protected static final List<String> YAML_NO_VALUES = Arrays.asList("n", "no", "false", "off");

    /// PROPERTY READERS

    protected static boolean getBoolean(Object obj) throws IOException {
        String value = getString(obj).trim().toLowerCase();
        if (YAML_YES_VALUES.contains(value)) {
            return true;
        }
        if (YAML_NO_VALUES.contains(value)) {
            return false;
        }
        throw new IOException("Invalid yaml boolean value: " + value);
    }

    protected static int getInt(Object obj) throws IOException {
        try {
            return Integer.parseInt(getString(obj));
        } catch (NumberFormatException e) {
            throw new IOException("Invalid yaml int value: " + obj);
        }
    }

    protected static float getFloat(Object obj) throws IOException {
        try {
            return Float.parseFloat(getString(obj));
        } catch (NumberFormatException e) {
            throw new IOException("Invalid yaml float value: " + obj);
        }
    }
    
    protected static <T extends Enum<T>> T getEnum(Object obj, Class<T> enumType) {
        String name = getString(obj);
        return Enum.valueOf(enumType, name);
    }

    protected static String getString(Object obj) {
        return obj.toString();
    }
    
    @SuppressWarnings("unchecked")
    protected static Map<String, Object> getMap(Object obj) throws IOException {
        if (obj instanceof Map<?, ?>) {
            return (Map<String, Object>) obj;
        } else {
            throw new IOException("Could not parse material. Expected Map at: " + obj);
        }
    }

    @SuppressWarnings("unchecked")
    protected static List<Object> getList(Object obj, int size) throws IOException {
        if (obj instanceof List<?>) {
            List<Object> list = (List<Object>) obj;
            if (list.size() != size) {
                throw new IOException("Invalid format, array must have " + size + " entries. Value: " + obj);
            }
            return list;
        } else {
            throw new IOException("Could not parse material. Expected List at: " + obj);
        }
    }

    protected static float[] parseFloatArray(Object doc, int size) throws IOException {
        List<Object> list = getList(doc, size);
        float[] a = new float[size];
        for (int i = 0; i < a.length; i++) {
            a[i] = getFloat(list.get(i));
        }
        return a;
    }

}
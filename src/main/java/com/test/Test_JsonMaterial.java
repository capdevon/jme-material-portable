package com.test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.TextureKey;
import com.jme3.material.MatParam;
import com.jme3.material.MatParamTexture;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.material.exporter.JsonMaterialExporter;
import com.jme3.material.json.JsonMatParam;
import com.jme3.material.json.JsonMaterial;
import com.jme3.material.json.JsonRenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.math.Vector4f;
import com.jme3.shader.VarType;
import com.jme3.system.JmeContext;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;

public class Test_JsonMaterial extends SimpleApplication {

    /**
     * @param args
     */
    public static void main(String[] args) {
        
//        String home = System.getProperty("user.dir");
//        String fileName = home + "/src/main/resources/MatDefs/Material2.json";
//        fromJson(fileName);
        
        Test_JsonMaterial app = new Test_JsonMaterial();
        app.start(JmeContext.Type.Headless);
    }

    @Override
    public void simpleInitApp() {
        Material mat = assetManager.loadMaterial("Models/Ferrari/Car.j3m");
//        toJson(mat, null);
        JsonMaterialExporter exporter = new JsonMaterialExporter();
        try {
            exporter.save(mat, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        stop();
    }
    
    /**
     */
//    public static void fromJson(String fileName) {
//        
//        try (FileReader reader = new FileReader(fileName)) {
//            Gson gson = new Gson();
//
//            // Deserialize JSON into a Material object
//            JsonMaterial material = gson.fromJson(reader, JsonMaterial.class);
//
//            // Access data from the Material object
//            System.out.println("Material Name: " + material.getName());
//            System.out.println("Material Definition: " + material.getDef());
//
//            // Access MaterialParameters list
//            List<JsonMatParam> parameters = material.getMaterialParameters();
//            for (JsonMatParam param : parameters) {
//                System.out.println(param);
//            }
//
//            // Access AdditionalRenderState
//            JsonRenderState renderState = material.getAdditionalRenderState();
//            System.out.println(renderState);
//
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }
    
    public static void toJson(Material mat, File f) {
        JsonObject data = new JsonObject();
        data.addProperty("name", mat.getName());
        data.addProperty("def", mat.getMaterialDef().getAssetName());

        JsonArray parameters = toJson(mat.getParams());
        data.add("materialParameters", parameters);

        JsonObject renderState = toJson(mat.getAdditionalRenderState());
        data.add("additionalRenderState", renderState);

        // Convert JsonObject to String
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonString = gson.toJson(data);
        System.out.println(jsonString);

        // Write JSON String to file
//        try (FileWriter writer = new FileWriter(f)) {
//            writer.write(jsonString);
//        }
    }

    /**
     * @param params
     * @return
     */
    private static JsonArray toJson(Collection<MatParam> params) {
        JsonArray parameters = new JsonArray();
        for (MatParam param : params) {
            JsonObject json = null;
            if (param instanceof MatParamTexture) {
                //formatMatParamTexture((MatParamTexture) param);
            } else {
                json = formatMatParam(param);
            }
            
            if (json != null) {
                parameters.add(json);
            }
        }
        return parameters;
    }

    /**
     * @param rs
     * @return
     */
    private static JsonObject toJson(RenderState rs) {
        JsonObject json = new JsonObject();
        json.addProperty("depthWrite", rs.isDepthWrite());
        json.addProperty("colorWrite", rs.isColorWrite());
        json.addProperty("depthTest", rs.isDepthTest());
        json.addProperty("wireframe", rs.isWireframe());
        json.addProperty("faceCull", rs.getFaceCullMode().name());
        json.addProperty("blend", rs.getBlendMode().name());
        
        JsonArray polyOffset = toJsonArray(rs.getPolyOffsetFactor(), rs.getPolyOffsetUnits());
        json.add("polyOffset", polyOffset);
        
        return json;
    }
    
    private static JsonObject formatMatParam(MatParam param) {
        JsonObject json = new JsonObject();
        json.addProperty("name", param.getName());
        
        VarType type = param.getVarType();
        Object val = param.getValue();
        
        switch (type) {
            case Int:
                json.addProperty("value", (Integer) val);
                break;
                
            case Float:
                json.addProperty("value", (Float) val);
                break;
                
            case Boolean:
                json.addProperty("value", (Boolean) val);
                break;
                
            case Vector2:
                Vector2f v2 = (Vector2f) val;
                json.add("value", toJsonArray(v2.toArray(null)));
                break;
                
            case Vector3:
                Vector3f v3 = (Vector3f) val;
                json.add("value", toJsonArray(v3.toArray(null)));
                break;
                
            case Vector4:
                // can be either ColorRGBA, Vector4f or Quaternion
                if (val instanceof Vector4f) {
                    Vector4f v4 = (Vector4f) val;
                    json.add("value", toJsonArray(v4.toArray(null)));
                    
                } else if (val instanceof ColorRGBA) {
                    ColorRGBA color = (ColorRGBA) val;
                    json.add("value", toJsonArray(color.toArray(null)));
                    
                } else if (val instanceof Quaternion) {
                    Quaternion q = (Quaternion) val;
                    float[] array = { q.getX(), q.getY(), q.getZ(), q.getW() };
                    json.add("value", toJsonArray(array));

                } else {
                    throw new UnsupportedOperationException("Unexpected Vector4 type: " + val);
                }
                break;

            default:
                return null; // parameter type not supported in J3M
        }
        return json;
    }
    
    private static JsonArray toJsonArray(float... values) {
        JsonArray jsonArray = new JsonArray();
        for (int i = 0; i < values.length; i++) {
            jsonArray.add(values[i]);
        }
        return jsonArray;
    }
    
    private JsonObject formatMatParamTexture(MatParamTexture param) {
        JsonObject json = new JsonObject();
        json.addProperty("name", param.getName());
        
        Texture tex = (Texture) param.getValue();
        if (tex != null) {
            
            TextureKey key = (TextureKey) tex.getKey();
            if (key != null) {
                json.addProperty("path", key.getName());
                
                if (key.isFlipY()) {
                    json.addProperty("flipY", true);
                }
            }

//            sb.append(formatWrapMode(tex, Texture.WrapAxis.S)); //TODO:
//            sb.append(formatWrapMode(tex, Texture.WrapAxis.T)); //TODO:
//            sb.append(formatWrapMode(tex, Texture.WrapAxis.R)); //TODO:

            //Min and Mag filter
            if (tex.getMinFilter() != Texture.MinFilter.Trilinear) {
                json.addProperty("minFilter", tex.getMinFilter().name());
            }
            if (tex.getMagFilter() != Texture.MagFilter.Bilinear) {
                json.addProperty("magFilter", tex.getMagFilter().name());
            }
        }

        return json;
    }

    private String formatWrapMode(Texture texVal, Texture.WrapAxis axis) {
        WrapMode mode;
        try {
            mode = texVal.getWrap(axis);
        } catch (IllegalArgumentException e) {
            // this axis doesn't exist on the texture
            return "";
        }
        if (mode != WrapMode.EdgeClamp) {
            return "Wrap" + mode.name() + "_" + axis.name() + " ";
        }
        return "";
    }

}
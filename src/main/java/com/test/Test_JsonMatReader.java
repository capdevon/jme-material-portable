package com.test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jme3.app.SimpleApplication;
import com.jme3.material.MatParam;
import com.jme3.material.MatParamTexture;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.material.exporter.JsonMaterialExporter;
import com.jme3.material.exporter.JsonMaterialExporter.JsonMatParam;
import com.jme3.material.exporter.JsonMaterialExporter.JsonMaterial;
import com.jme3.material.exporter.JsonMaterialExporter.JsonRenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.math.Vector4f;
import com.jme3.shader.VarType;
import com.jme3.system.JmeContext;

public class Test_JsonMatReader extends SimpleApplication {

    /**
     * 
     * @param args
     */
    public static void main(String[] args) {
        
//        String home = System.getProperty("user.dir");
//        String fileName = home + "/src/main/resources/MatDefs/Material2.json";
//        fromJson(fileName);
        
        Test_JsonMatReader app = new Test_JsonMatReader();
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
    public static void fromJson(String fileName) {
        
        try (FileReader reader = new FileReader(fileName)) {
            Gson gson = new Gson();

            // Deserialize JSON into a Material object
            JsonMaterial material = gson.fromJson(reader, JsonMaterial.class);

            // Access data from the Material object
            System.out.println("Material Name: " + material.getName());
            System.out.println("Material Definition: " + material.getDef());

            // Access MaterialParameters list
            List<JsonMatParam> parameters = material.getMaterialParameters();
            for (JsonMatParam param : parameters) {
                System.out.println(param);
            }

            // Access AdditionalRenderState
            JsonRenderState renderState = material.getAdditionalRenderState();
            System.out.println(renderState);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public static void toJson(Material mat, File f) {
        JsonObject data = new JsonObject();
        data.addProperty("name", mat.getName());
        data.addProperty("def", mat.getMaterialDef().getAssetName());

        JsonArray parameters = new JsonArray();
        for (MatParam param : mat.getParams()) {
            JsonObject obj = null;
            if (param instanceof MatParamTexture) {
                //formatMatParamTexture((MatParamTexture) param);
            } else {
                obj = formatMatParam(param);
            }
            
            if (obj != null) {
                parameters.add(obj);
            }
        }
        data.add("materialParameters", parameters);

        RenderState rs = mat.getAdditionalRenderState();
        JsonObject renderState = new JsonObject();
        renderState.addProperty("depthWrite", rs.isDepthWrite());
        renderState.addProperty("colorWrite", rs.isColorWrite());
        renderState.addProperty("depthTest", rs.isDepthTest());
        renderState.addProperty("wireframe", rs.isWireframe());
        renderState.addProperty("faceCull", rs.getFaceCullMode().name());
        renderState.addProperty("blend", rs.getBlendMode().name());
        JsonArray polyOffset = toJsonArray(rs.getPolyOffsetFactor(), rs.getPolyOffsetUnits());
        renderState.add("polyOffset", polyOffset);
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
    
    private static JsonObject formatMatParam(MatParam param) {
        JsonObject jsonObj = new JsonObject();
        jsonObj.addProperty("name", param.getName());
        
        VarType type = param.getVarType();
        Object val = param.getValue();
        
        switch (type) {
            case Boolean:
                jsonObj.addProperty("value", (Boolean) val);
                break;
                
            case Float:
                jsonObj.addProperty("value", (Float) val);
                break;
                
            case Int:
                jsonObj.addProperty("value", (Integer) val);
                break;
                
            case Vector2:
                Vector2f v2 = (Vector2f) val;
                jsonObj.add("value", toJsonArray(v2.toArray(null)));
                break;
                
            case Vector3:
                Vector3f v3 = (Vector3f) val;
                jsonObj.add("value", toJsonArray(v3.toArray(null)));
                break;
                
            case Vector4:
                // can be either ColorRGBA, Vector4f or Quaternion
                if (val instanceof Vector4f) {
                    Vector4f v4 = (Vector4f) val;
                    jsonObj.add("value", toJsonArray(v4.toArray(null)));
                    
                } else if (val instanceof ColorRGBA) {
                    ColorRGBA color = (ColorRGBA) val;
                    jsonObj.add("value", toJsonArray(color.toArray(null)));
                    
                } else if (val instanceof Quaternion) {
                    Quaternion q = (Quaternion) val;
                    float[] array = { q.getX(), q.getY(), q.getZ(), q.getW() };
                    jsonObj.add("value", toJsonArray(array));

                } else {
                    throw new UnsupportedOperationException("Unexpected Vector4 type: " + val);
                }
                break;

            default:
                return null; // parameter type not supported in J3M
        }
        return jsonObj;
    }
    
    private static JsonArray toJsonArray(float... values) {
        JsonArray jsonArray = new JsonArray();
        for (int i = 0; i < values.length; i++) {
            jsonArray.add(values[i]);
        }
        return jsonArray;
    }

}
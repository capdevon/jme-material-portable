package com.jme3.material.exporter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jme3.asset.TextureKey;
import com.jme3.material.MatParam;
import com.jme3.material.MatParamTexture;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.material.utils.MaterialUtils;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.math.Vector4f;
import com.jme3.shader.VarType;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;

/**
 * 
 * @author capdevon
 */
public class JsonMaterialExporter2 {
    
    private static final Logger logger = Logger.getLogger(JsonMaterialExporter2.class.getName());
        
    public void save(Material mat, File f) throws IOException {
        JsonObject jsonMaterial = toJson(mat);
        
        // Convert JsonObject to String
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonString = gson.toJson(jsonMaterial);
        logger.log(Level.INFO, jsonString);

        // Write JSON String to file
        try (FileWriter writer = new FileWriter(f)) {
            writer.write(jsonString);
        }
    }

    private JsonObject toJson(Material mat) {
        JsonObject data = new JsonObject();
        data.addProperty("name", mat.getName());
        data.addProperty("def", mat.getMaterialDef().getAssetName());

        JsonArray parameters = toJson(MaterialUtils.sortMatParams(mat.getParams()));
        data.add("materialParameters", parameters);

        JsonObject renderState = toJson(mat.getAdditionalRenderState());
        data.add("additionalRenderState", renderState);

        JsonObject jsonMaterial = new JsonObject();
        jsonMaterial.add("material", data);
        return jsonMaterial;
    }

    private JsonArray toJson(Collection<MatParam> params) {
        JsonArray parameters = new JsonArray();
        for (MatParam param : params) {
            if (param instanceof MatParamTexture) {
                JsonObject json = formatMatParamTexture((MatParamTexture) param);
                parameters.add(json);
            } else {
                JsonObject json = formatMatParam(param);
                parameters.add(json);
            }
        }
        return parameters;
    }

    private JsonObject toJson(RenderState rs) {
        JsonObject json = new JsonObject();
        RenderState defRs = RenderState.DEFAULT;
        
        if (rs.getBlendMode() != defRs.getBlendMode()) {
            json.addProperty("blend", rs.getBlendMode().name());
        }
        if (rs.isWireframe() != defRs.isWireframe()) {
            json.addProperty("wireframe", rs.isWireframe());
        }
        if (rs.getFaceCullMode() != defRs.getFaceCullMode()) {
            json.addProperty("faceCull", rs.getFaceCullMode().name());
        }
        if (rs.isDepthWrite() != defRs.isDepthWrite()) {
            json.addProperty("depthWrite", rs.isDepthWrite());
        }
        if (rs.isDepthTest() != defRs.isDepthTest()) {
            json.addProperty("depthTest", rs.isDepthTest());
        }
//        if (rs.getBlendEquation() != defRs.getBlendEquation()) {
//            json.addProperty("blendEquation", rs.getBlendEquation().name());
//        }
//        if (rs.getBlendEquationAlpha() != defRs.getBlendEquationAlpha()) {
//            json.addProperty("blendEquationAlpha", rs.getBlendEquationAlpha().name());
//        }
        if (rs.isColorWrite() != defRs.isColorWrite()) {
            json.addProperty("colorWrite", rs.isColorWrite());
        }
        if (rs.getDepthFunc() != defRs.getDepthFunc()) {
            json.addProperty("depthFunc", rs.getDepthFunc().name());
        }
        if (rs.getLineWidth() != defRs.getLineWidth()) {
            json.addProperty("lineWidth", Float.toString(rs.getLineWidth()));
        }
        
        if (rs.getPolyOffsetFactor() != defRs.getPolyOffsetFactor()
                || rs.getPolyOffsetUnits() != defRs.getPolyOffsetUnits()) {
            json.add("polyOffset", toJsonArray(rs.getPolyOffsetFactor(), rs.getPolyOffsetUnits()));
        }
        
        return json;
    }
    
    private JsonObject formatMatParam(MatParam param) {
        JsonObject jsonParam = new JsonObject();
        jsonParam.addProperty("name", param.getName());
        
        VarType type = param.getVarType();
        Object val = param.getValue();
        
        switch (type) {
            case Int:
                jsonParam.addProperty("value", (Integer) val);
                break;
                
            case Float:
                jsonParam.addProperty("value", (Float) val);
                break;
                
            case Boolean:
                jsonParam.addProperty("value", (Boolean) val);
                break;
                
            case Vector2:
                Vector2f v2 = (Vector2f) val;
                jsonParam.add("value", toJsonArray(v2.toArray(null)));
                break;
                
            case Vector3:
                Vector3f v3 = (Vector3f) val;
                jsonParam.add("value", toJsonArray(v3.toArray(null)));
                break;
                
            case Vector4:
                if (val instanceof Vector4f) {
                    Vector4f v4 = (Vector4f) val;
                    jsonParam.add("value", toJsonArray(v4.toArray(null)));
                    
                } else if (val instanceof ColorRGBA) {
                    ColorRGBA color = (ColorRGBA) val;
                    jsonParam.add("value", toJsonArray(color.toArray(null)));

                } else {
                    throw new UnsupportedOperationException("Unexpected Vector4 type: " + val);
                }
                break;

            default:
                throw new UnsupportedOperationException("Parameter type not supported in J3M: " + type);
        }
        return jsonParam;
    }
    
    private JsonObject formatMatParamTexture(MatParamTexture param) {
        JsonObject jsonParam = new JsonObject();
        jsonParam.addProperty("name", param.getName());
        
        Texture tex = (Texture) param.getValue();
        if (tex != null) {
            
            JsonObject jsonTexture = new JsonObject();
            jsonParam.add("texture", jsonTexture);
            
            TextureKey key = (TextureKey) tex.getKey();
            if (key != null) {
                jsonTexture.addProperty("path", key.getName());
                
                if (key.isFlipY()) {
                    jsonTexture.addProperty("flipY", true);
                }
            }

            Texture.WrapAxis[] axis = { Texture.WrapAxis.S, Texture.WrapAxis.T, Texture.WrapAxis.R };
            for (int i = 0; i < axis.length; i++) {
                WrapMode wrapMode = formatWrapMode(tex, axis[i]);
                if (wrapMode != null) {
                    jsonTexture.addProperty("wrap" + axis[i].name(), wrapMode.name());
                }
            }

            //Min and Mag filter
            if (tex.getMinFilter() != Texture.MinFilter.Trilinear) {
                jsonTexture.addProperty("minFilter", tex.getMinFilter().name());
            }
            if (tex.getMagFilter() != Texture.MagFilter.Bilinear) {
                jsonTexture.addProperty("magFilter", tex.getMagFilter().name());
            }
        }

        return jsonParam;
    }

    private WrapMode formatWrapMode(Texture texVal, Texture.WrapAxis axis) {
        WrapMode mode;
        try {
            mode = texVal.getWrap(axis);
        } catch (IllegalArgumentException e) {
            // this axis doesn't exist on the texture
            return null;
        }
        if (mode != WrapMode.EdgeClamp) {
            return mode;
        }
        return null;
    }
    
    private JsonArray toJsonArray(float... values) {
        JsonArray array = new JsonArray();
        for (int i = 0; i < values.length; i++) {
            array.add(values[i]);
        }
        return array;
    }
    
}
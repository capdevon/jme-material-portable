package com.jme3.material.exporter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jme3.asset.TextureKey;
import com.jme3.material.MatParam;
import com.jme3.material.MatParamTexture;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.material.json.JsonMatParam;
import com.jme3.material.json.JsonMaterial;
import com.jme3.material.json.JsonRenderState;
import com.jme3.material.json.JsonTexture;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.math.Vector4f;
import com.jme3.shader.VarType;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;

public class JsonMaterialExporter { //implements JmeExporter {
        
    /**
     * 
     * @param material
     * @param f
     * @throws IOException
     */
    public void save(Material material, File f) throws IOException {
        JsonMaterial mat = new JsonMaterial();
        mat.setName(material.getName());
        mat.setDef(material.getMaterialDef().getAssetName());

        List<JsonMatParam> parameters = toJson(material.getParams());
        mat.setMaterialParameters(parameters);

        JsonRenderState renderState = toJson(material.getAdditionalRenderState());
        mat.setAdditionalRenderState(renderState);

        // Convert JsonObject to String
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonString = gson.toJson(mat);
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
    private List<JsonMatParam> toJson(Collection<MatParam> params) {
        List<JsonMatParam> parameters = new ArrayList<>();
        for (MatParam param : params) {
            
            JsonMatParam json = new JsonMatParam();
            json.name = param.getName();
            
            if (param instanceof MatParamTexture) {
                json.texture = formatMatParamTexture((MatParamTexture) param);
            } else {
                json.value = formatMatParam(param);
            }
            
            parameters.add(json);
        }
        return parameters;
    }

    /**
     * @param rs
     * @return
     */
    private JsonRenderState toJson(RenderState rs) {
        JsonRenderState json = new JsonRenderState();
        json.faceCull = rs.getFaceCullMode();
        json.blend = rs.getBlendMode();
        json.wireframe = rs.isWireframe();
        json.depthWrite = rs.isDepthWrite();
        json.colorWrite = rs.isColorWrite();
        json.depthTest = rs.isDepthTest();
        json.polyOffset = new float[] { rs.getPolyOffsetFactor(), rs.getPolyOffsetUnits() };
        return json;
    }
    
    private Object formatMatParam(MatParam param) {
        VarType type = param.getVarType();
        Object val = param.getValue();
        
        switch (type) {
            case Boolean:
            case Float:
            case Int:
                return val;
                
            case Vector2:
                Vector2f vec2 = (Vector2f) val;
                return vec2.toArray(null);
                
            case Vector3:
                Vector3f vec3 = (Vector3f) val;
                return vec3.toArray(null);
                
            case Vector4:
                if (val instanceof Vector4f) {
                    Vector4f vec4 = (Vector4f) val;
                    return vec4.toArray(null);

                } else if (val instanceof ColorRGBA) {
                    ColorRGBA color = (ColorRGBA) val;
                    return color.toArray(null);

                } else {
                    throw new UnsupportedOperationException("Unexpected Vector4 type: " + val);
                }

            default:
                return null; // parameter type not supported in J3M
        }
    }

    private JsonTexture formatMatParamTexture(MatParamTexture param) {
        JsonTexture json = new JsonTexture();
        Texture tex = (Texture) param.getValue();
        if (tex != null) {
            
            TextureKey key = (TextureKey) tex.getKey();
            if (key != null) {
                json.path = key.getName();
                
                if (key.isFlipY()) {
                    json.flipY = true;
                }
            }

//            sb.append(formatWrapMode(tex, Texture.WrapAxis.S));
//            sb.append(formatWrapMode(tex, Texture.WrapAxis.T));
//            sb.append(formatWrapMode(tex, Texture.WrapAxis.R));

            //Min and Mag filter
            if (tex.getMinFilter() != Texture.MinFilter.Trilinear) {
                json.minFilter = tex.getMinFilter();
            }
            if (tex.getMagFilter() != Texture.MagFilter.Bilinear) {
                json.magFilter = tex.getMagFilter();
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

//    @Override
//    public void save(Savable object, OutputStream f) throws IOException {
//        if (!(object instanceof Material)) {
//            throw new IllegalArgumentException("J3MExporter can only save com.jme3.material.Material class");
//        }
//
//        try (OutputStreamWriter out = new OutputStreamWriter(f, StandardCharsets.UTF_8)) {
//            object.write(this);
//        }
//    }
//
//    @Override
//    public void save(Savable object, File f) throws IOException {
//        File parentDir = f.getParentFile();
//        if (parentDir != null && !parentDir.exists()) {
//            parentDir.mkdirs();
//        }
//
//        try (FileOutputStream fos = new FileOutputStream(f); BufferedOutputStream bos = new BufferedOutputStream(fos)) {
//            save(object, bos);
//        }
//    }
//
//    @Override
//    public OutputCapsule getCapsule(Savable object) {
//        return null;
//    }
    
}
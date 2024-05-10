package com.jme3.material.exporter;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jme3.material.MatParam;
import com.jme3.material.MatParamTexture;
import com.jme3.material.MaterialDef;
import com.jme3.material.RenderState;
import com.jme3.material.TechniqueDef;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.math.Vector4f;
import com.jme3.shader.Shader;
import com.jme3.shader.UniformBinding;
import com.jme3.texture.image.ColorSpace;

/**
 * https://github.com/jMonkeyEngine/jmonkeyengine/blob/master/jme3-plugins/src/main/java/com/jme3/material/plugin/export/materialdef/J3mdExporter.java
 * @author capdevon
 */
public class JsonMatDefExporter {

    /**
     * 
     * @param matDef
     * @param f
     * @throws IOException
     */
    public void save(MaterialDef matDef, File f) throws IOException {
        
        JsonObject data = new JsonObject();
        data.addProperty("name", matDef.getName());
        
        Collection<MatParam> matParams = matDef.getMaterialParams();
        JsonArray parameters = new JsonArray();
        for (MatParam param : matParams) {
            parameters.add(write(param));
        }
        data.add("MaterialParameters", parameters);

        JsonArray tenchiques = new JsonArray();
        for (String defName : matDef.getTechniqueDefsNames()) {
            List<TechniqueDef> defs = matDef.getTechniqueDefs(defName);
            for (TechniqueDef techniqueDef : defs) {
                tenchiques.add(writeTechnique(techniqueDef, matParams));
            }
        }
        data.add("Techniques", tenchiques);
        System.out.println(data);
        
        // Write JSON String to file
//      try (FileWriter writer = new FileWriter(f)) {
//          writer.write(jsonString);
//      }
    }

    private JsonObject write(MatParam param) {
        JsonObject json = new JsonObject();
        json.addProperty("type", param.getVarType().name());
        json.addProperty("name", param.getName());

        if (param instanceof MatParamTexture) {
            MatParamTexture paramTex = (MatParamTexture) param;
            if (paramTex.getColorSpace() == ColorSpace.Linear) {
                json.addProperty("colorSpace", "Linear");
            }
        }
        
        Object val = param.getValue();
        if (val != null) {
            switch (param.getVarType()) {
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
                    if (val instanceof Vector4f) {
                        Vector4f v4 = (Vector4f) val;
                        json.add("value", toJsonArray(v4.toArray(null)));
    
                    } else if (val instanceof ColorRGBA) {
                        ColorRGBA color = (ColorRGBA) val;
                        json.add("value", toJsonArray(color.toArray(null)));
                    }
                    break;
                    
                default:
                    break;
            }
        }

        return json;
    }

    /**
     * @param techniqueDef
     * @param matParams
     */
    private JsonObject writeTechnique(TechniqueDef techniqueDef, Collection<MatParam> matParams) {
        JsonObject json = new JsonObject();
        json.addProperty("name", techniqueDef.getName());
        
        writeShaders(techniqueDef, json);

        // Light mode
        if (techniqueDef.getLightMode() != TechniqueDef.LightMode.Disable) {
            json.addProperty("LightMode", techniqueDef.getLightMode().name());
        }
        // Shadow mode
        if (techniqueDef.getShadowMode() != TechniqueDef.ShadowMode.Disable) {
            json.addProperty("ShadowMode", techniqueDef.getShadowMode().name());
        }
        // World params
        if (!techniqueDef.getWorldBindings().isEmpty()) {
            JsonArray parameters = writeWorldParams(techniqueDef);
            json.add("WorldParameters", parameters);
        }
        // Defines
        if (techniqueDef.getDefineNames().length != 0) {
            JsonArray defines = writeDefines(techniqueDef, matParams);
            json.add("Defines", defines);
        }

        // render state
        RenderState rs = techniqueDef.getRenderState();
        if (rs != null) {
            json.add("RenderState", writeRenderState(rs));
        }

        // forced render state
        rs = techniqueDef.getForcedRenderState();
        if (rs != null) {
            json.add("ForcedRenderState", writeRenderState(rs));
        }

        // no render
        if (techniqueDef.isNoRender()) {
            json.addProperty("NoRender", true);
        }
        
        return json;
    }

    private void writeShaders(TechniqueDef techniqueDef, JsonObject out) {
        if (techniqueDef.getShaderProgramNames().size() > 0) {
            for (Shader.ShaderType shaderType : techniqueDef.getShaderProgramNames().keySet()) {
                out.addProperty(shaderType.name() + "Shader", techniqueDef.getShaderProgramNames().get(shaderType));
                out.addProperty("ShaderLanguages", techniqueDef.getShaderProgramLanguage(shaderType));
            }
        }
    }
    
    /**
     * @param techniqueDef
     * @return
     */
    private JsonArray writeWorldParams(TechniqueDef techniqueDef) {
        JsonArray array = new JsonArray();
        for (UniformBinding uniformBinding : techniqueDef.getWorldBindings()) {
            array.add(uniformBinding.toString());
        }
        return array;
    }
    
    /**
     * @param technique
     * @param matParams
     * @return
     */
    private JsonArray writeDefines(TechniqueDef technique, Collection<MatParam> matParams) {
        JsonArray array = new JsonArray();
        for (int i = 0; i < technique.getDefineNames().length; i++) {
            String matParamName = getMatParamNameForDefineId(technique, matParams, i);
            if (matParamName != null) {
                String defineName = technique.getDefineNames()[i];
                JsonObject json = new JsonObject();
                json.addProperty("name", defineName);
                json.addProperty("param", matParamName);
                array.add(json);
            }
        }
        return array;
    }
    
    /**
     * @param rs
     * @return
     */
    private JsonObject writeRenderState(RenderState rs) {
        
        JsonObject json = new JsonObject();
        RenderState defRs = RenderState.DEFAULT;
        
        if (rs.getBlendMode() != defRs.getBlendMode()) {
            json.addProperty("Blend", rs.getBlendMode().name());
        }
        if (rs.isWireframe() != defRs.isWireframe()) {
            json.addProperty("Wireframe", rs.isWireframe());
        }
        if (rs.getFaceCullMode() != defRs.getFaceCullMode()) {
            json.addProperty("FaceCull", rs.getFaceCullMode().name());
        }
        if (rs.isDepthWrite() != defRs.isDepthWrite()) {
            json.addProperty("DepthWrite", rs.isDepthWrite());
        }
        if (rs.isDepthTest() != defRs.isDepthTest()) {
            json.addProperty("DepthTest", rs.isDepthTest());
        }
        if (rs.getBlendEquation() != defRs.getBlendEquation()) {
            json.addProperty("BlendEquation", rs.getBlendEquation().name());
        }
        if (rs.getBlendEquationAlpha() != defRs.getBlendEquationAlpha()) {
            json.addProperty("BlendEquationAlpha", rs.getBlendEquationAlpha().name());
        }
        if (rs.isColorWrite() != defRs.isColorWrite()) {
            json.addProperty("ColorWrite", rs.isColorWrite());
        }
        if (rs.getDepthFunc() != defRs.getDepthFunc()) {
            json.addProperty("DepthFunc", rs.getDepthFunc().name());
        }
        if (rs.getLineWidth() != defRs.getLineWidth()) {
            json.addProperty("LineWidth", Float.toString(rs.getLineWidth()));
        }
        
        if (rs.getPolyOffsetFactor() != defRs.getPolyOffsetFactor()
                || rs.getPolyOffsetUnits() != defRs.getPolyOffsetUnits()) {
            json.add("PolyOffset", toJsonArray(rs.getPolyOffsetFactor(), rs.getPolyOffsetUnits()));
        }
        
        return json;
    }
    
    private String getMatParamNameForDefineId(TechniqueDef techniqueDef, Collection<MatParam> matParams, int defineId) {
        for (MatParam matParam : matParams) {
            Integer id = techniqueDef.getShaderParamDefineId(matParam.getName());
            if (id != null && id == defineId) {
                return matParam.getName();
            }
        }
        return null;
    }
    
    private static JsonArray toJsonArray(float... values) {
        JsonArray jsonArray = new JsonArray();
        for (int i = 0; i < values.length; i++) {
            jsonArray.add(values[i]);
        }
        return jsonArray;
    }
    
}

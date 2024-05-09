package com.jme3.material.exporter;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jme3.material.MatParam;
import com.jme3.material.MaterialDef;
import com.jme3.material.RenderState;
import com.jme3.material.TechniqueDef;
import com.jme3.shader.UniformBinding;

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
        StringBuilder sb = new StringBuilder();
        sb.append("name : ").append(matDef.getName()).append("\n");
        Collection<MatParam> matParams = matDef.getMaterialParams();
        
//        for (MatParam param : matParams) {
//            sb.append(param);
//            if (param instanceof MatParamTexture) {
//                sb.append(" ").append(((MatParamTexture) param).getColorSpace());
//            }
//            sb.append("\n");
//        }
//        System.out.println(sb.toString());

        for (String defName : matDef.getTechniqueDefsNames()) {
            List<TechniqueDef> defs = matDef.getTechniqueDefs(defName);
            for (TechniqueDef techniqueDef : defs) {

                JsonObject data = new JsonObject();
                data.addProperty("name", techniqueDef.getName());

                // Light mode
                if (techniqueDef.getLightMode() != TechniqueDef.LightMode.Disable) {
                    data.addProperty("LightMode", techniqueDef.getLightMode().name());
                }
                // Shadow mode
                if (techniqueDef.getShadowMode() != TechniqueDef.ShadowMode.Disable) {
                    data.addProperty("ShadowMode", techniqueDef.getShadowMode().name());
                }
                // World params
                if (!techniqueDef.getWorldBindings().isEmpty()) {
                    JsonArray parameters = writeWorldParams(techniqueDef);
                    data.add("WorldParameters", parameters);
                }
                // Defines
                if (techniqueDef.getDefineNames().length != 0) {
                    JsonArray defines = writeDefines(techniqueDef, matParams);
                    data.add("Defines", defines);
                }

                // render state
                RenderState rs = techniqueDef.getRenderState();
                if (rs != null) {
                    data.add("RenderState", writeRenderState(rs));
                }

                // forced render state
                rs = techniqueDef.getForcedRenderState();
                if (rs != null) {
                    data.add("ForcedRenderState", writeRenderState(rs));
                }

                // no render
                if (techniqueDef.isNoRender()) {
                    data.addProperty("NoRender", true);
                }
            }
        }
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
//            json.addProperty("PolyOffset", rs.getPolyOffsetFactor() + " " + rs.getPolyOffsetUnits());
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
    
}

package com.jme3.material.exporter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jme3.material.MatParam;
import com.jme3.material.MatParamTexture;
import com.jme3.material.MaterialDef;
import com.jme3.material.RenderState;
import com.jme3.material.TechniqueDef;
import com.jme3.material.utils.MaterialUtils;
import com.jme3.material.utils.StringUtils;
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
     * Saves the given MaterialDef object to a specified file in JSON format.
     *
     * @param matDef the MaterialDef object to be saved
     * @param file   the file to which the JSON representation of the MaterialDef
     *               object will be written
     * @throws IOException if an I/O error occurs during writing to the file
     */
    public void save(MaterialDef matDef, File file) throws IOException {

        JsonObject data = writeMaterialDef(matDef);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonString = gson.toJson(data);

        // Write JSON String to file
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(jsonString);
        }
    }
    
    /**
     * Saves the given MaterialDef object to the specified OutputStream in JSON
     * format.
     *
     * @param matDef the MaterialDef object to be saved
     * @param out    the OutputStream to which the JSON representation of the
     *               MaterialDef object will be written
     * @throws IOException if an I/O error occurs during writing
     */
    public void save(MaterialDef matDef, OutputStream out) throws IOException {

        JsonObject data = writeMaterialDef(matDef);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonString = gson.toJson(data);

        try (OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
            writer.write(jsonString);
        }
    }

    private JsonObject writeMaterialDef(MaterialDef matDef) {
        JsonObject data = new JsonObject();
        data.addProperty("name", matDef.getName());

        Collection<MatParam> matParams = MaterialUtils.sortMatParams(matDef.getMaterialParams());
        JsonArray parameters = new JsonArray();
        for (MatParam param : matParams) {
            parameters.add(writeMatParam(param));
        }
        data.add("materialParameters", parameters);

        JsonArray tenchiques = new JsonArray();
        for (String defName : matDef.getTechniqueDefsNames()) {
            List<TechniqueDef> defs = matDef.getTechniqueDefs(defName);
            for (TechniqueDef techniqueDef : defs) {
                tenchiques.add(writeTechnique(techniqueDef, matParams));
                //break;
            }
        }
        data.add("techniques", tenchiques);
        
        JsonObject jsonObj = new JsonObject();
        jsonObj.add("materialDef", data);
        
        return jsonObj;
    }

    private JsonObject writeMatParam(MatParam param) {
        JsonObject json = new JsonObject();
        json.addProperty("type", param.getVarType().name());
        json.addProperty("name", param.getName());

        if (param instanceof MatParamTexture) {
            MatParamTexture paramTex = (MatParamTexture) param;
            if (paramTex.getColorSpace() == ColorSpace.Linear) {
                json.addProperty("colorSpace", "Linear");
            }
        }
        
        Object paramValue = param.getValue();
        if (paramValue != null) {
            switch (param.getVarType()) {
                case Int:
                    json.addProperty("value", (Integer) paramValue);
                    break;
                    
                case Float:
                    json.addProperty("value", (Float) paramValue);
                    break;
                    
                case Boolean:
                    json.addProperty("value", (Boolean) paramValue);
                    break;
    
                case Vector2:
                    Vector2f v2 = (Vector2f) paramValue;
                    json.add("value", toJsonArray(v2.toArray(null)));
                    break;
    
                case Vector3:
                    Vector3f v3 = (Vector3f) paramValue;
                    json.add("value", toJsonArray(v3.toArray(null)));
                    break;
    
                case Vector4:
                    if (paramValue instanceof Vector4f) {
                        Vector4f v4 = (Vector4f) paramValue;
                        json.add("value", toJsonArray(v4.toArray(null)));
    
                    } else if (paramValue instanceof ColorRGBA) {
                        ColorRGBA color = (ColorRGBA) paramValue;
                        json.add("value", toJsonArray(color.toArray(null)));
                    }
                    break;
                    
                default:
                    break;
            }
        }

        return json;
    }

    private JsonObject writeTechnique(TechniqueDef techniqueDef, Collection<MatParam> matParams) {
        JsonObject json = new JsonObject();
        json.addProperty("name", techniqueDef.getName());
        
        writeShaders(techniqueDef, json);

        // Light mode
        if (techniqueDef.getLightMode() != TechniqueDef.LightMode.Disable) {
            json.addProperty("lightMode", techniqueDef.getLightMode().name());
        }
        // Shadow mode
        if (techniqueDef.getShadowMode() != TechniqueDef.ShadowMode.Disable) {
            json.addProperty("shadowMode", techniqueDef.getShadowMode().name());
        }
        // World params
        if (!techniqueDef.getWorldBindings().isEmpty()) {
            JsonArray parameters = writeWorldParams(techniqueDef);
            json.add("worldParameters", parameters);
        }
        // Defines
        if (techniqueDef.getDefineNames().length != 0) {
            JsonArray defines = writeDefines(techniqueDef, matParams);
            json.add("defines", defines);
        }

        // render state
        RenderState rs = techniqueDef.getRenderState();
        if (rs != null) {
            json.add("renderState", writeRenderState(rs));
        }

        // forced render state
        rs = techniqueDef.getForcedRenderState();
        if (rs != null) {
            json.add("forcedRenderState", writeRenderState(rs));
        }

        // no render
        if (techniqueDef.isNoRender()) {
            json.addProperty("noRender", true);
        }
        
        return json;
    }

    private void writeShaders(TechniqueDef techniqueDef, JsonObject out) {
        if (techniqueDef.getShaderProgramNames().size() > 0) {
            for (Shader.ShaderType shaderType : techniqueDef.getShaderProgramNames().keySet()) {
                String shaderName = StringUtils.uncapitalize(shaderType.name());
                out.addProperty(shaderName + "Shader", techniqueDef.getShaderProgramNames().get(shaderType));
                out.addProperty("shaderLanguages", techniqueDef.getShaderProgramLanguage(shaderType));
            }
        }
    }
    
    private JsonArray writeWorldParams(TechniqueDef techniqueDef) {
        JsonArray array = new JsonArray();
        for (UniformBinding uniformBinding : techniqueDef.getWorldBindings()) {
            array.add(uniformBinding.toString());
        }
        return array;
    }
    
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
    
    private JsonObject writeRenderState(RenderState rs) {
        
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
        if (rs.getBlendEquation() != defRs.getBlendEquation()) {
            json.addProperty("blendEquation", rs.getBlendEquation().name());
        }
        if (rs.getBlendEquationAlpha() != defRs.getBlendEquationAlpha()) {
            json.addProperty("blendEquationAlpha", rs.getBlendEquationAlpha().name());
        }
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
    
    private String getMatParamNameForDefineId(TechniqueDef techniqueDef, Collection<MatParam> matParams, int defineId) {
        for (MatParam matParam : matParams) {
            Integer id = techniqueDef.getShaderParamDefineId(matParam.getName());
            if (id != null && id == defineId) {
                return matParam.getName();
            }
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

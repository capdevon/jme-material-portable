package com.jme3.material.json;

import java.util.List;

/**
 * Define classes to represent the JSON structure
 * 
 * @author capdevon
 */
public class JsonMaterial {

    private String name;
    private String def;
    private List<JsonMatParam> materialParameters;
    private JsonRenderState additionalRenderState;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDef() {
        return def;
    }

    public void setDef(String def) {
        this.def = def;
    }

    public List<JsonMatParam> getMaterialParameters() {
        return materialParameters;
    }

    public void setMaterialParameters(List<JsonMatParam> materialParameters) {
        this.materialParameters = materialParameters;
    }

    public JsonRenderState getAdditionalRenderState() {
        return additionalRenderState;
    }

    public void setAdditionalRenderState(JsonRenderState additionalRenderState) {
        this.additionalRenderState = additionalRenderState;
    }

}
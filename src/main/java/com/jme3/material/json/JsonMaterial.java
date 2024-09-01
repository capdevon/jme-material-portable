package com.jme3.material.json;

import java.util.List;

/**
 * Define classes to represent the JSON structure
 * 
 * @author capdevon
 */
public class JsonMaterial {

    public String name;
    public String def;
    public List<JsonMatParam> materialParameters;
    public JsonRenderState additionalRenderState;

}
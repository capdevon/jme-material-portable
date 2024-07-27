package com.jme3.material.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.jme3.texture.image.ColorSpace;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonMatParam {
    
    public String name;
    public Object value; // Can be primitive or object
    public JsonTexture texture;
    public ColorSpace colorSpace;

    @Override
    public String toString() {
        return "MaterialParameter [name=" + name 
                + ", value=" + value 
                + ", texture=" + texture 
                + ", colorSpace=" + colorSpace
                + "]";
    }

}
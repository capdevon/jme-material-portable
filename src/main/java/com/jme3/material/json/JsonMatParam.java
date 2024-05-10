package com.jme3.material.json;

import com.jme3.texture.image.ColorSpace;

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
                + "]";
    }

}
package com.jme3.material.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.jme3.texture.Texture.MagFilter;
import com.jme3.texture.Texture.MinFilter;
import com.jme3.texture.Texture.WrapMode;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonTexture {
    
    public String path;
    public Boolean flipY;
    public WrapMode wrapS;
    public WrapMode wrapT;
    public WrapMode wrapR;
    public MinFilter minFilter;
    public MagFilter magFilter;
    
    @Override
    public String toString() {
        return "JsonTexture [path=" + path 
                + ", flipY=" + flipY 
                + ", wrapS=" + wrapS 
                + ", wrapT=" + wrapT 
                + ", wrapR=" + wrapR 
                + ", minFilter=" + minFilter 
                + ", magFilter=" + magFilter 
                + "]";
    }
    
}
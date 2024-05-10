package com.jme3.material.json;

import java.util.List;

import com.jme3.material.TechniqueDef.LightMode;
import com.jme3.material.TechniqueDef.LightSpace;
import com.jme3.material.TechniqueDef.ShadowMode;

public class JsonTechnique {

    public String name;
    public String fragmentShader;
    public String vertexShader;
    public List<String> shaderLanguages;
    public List<String> worldParameters;
    public List<JsonDefine> defines;
    public JsonRenderState renderState;
    public JsonRenderState forcedRenderState;
    
    public LightMode lightMode = LightMode.Disable;
    public ShadowMode shadowMode = ShadowMode.Disable;
    public LightSpace lightSpace;
    public boolean noRender = false;
}

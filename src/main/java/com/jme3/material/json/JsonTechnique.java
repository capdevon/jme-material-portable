package com.jme3.material.json;

import java.util.List;

public class JsonTechnique {

    public String name;
    public String fragmentShader;
    public String vertexShader;
    public List<String> shaderLanguages;
    public List<String> worldParameters;
    public List<JsonDefine> defines;
    public JsonRenderState renderState;
    public JsonRenderState forcedRenderState;
    
}

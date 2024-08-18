package com.jme3.material.json;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.material.RenderState.TestFunction;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonRenderState {
    
    public FaceCullMode faceCull;
    public BlendMode blend;
    public Boolean depthTest;
    public Boolean depthWrite;
    public Boolean colorWrite;
    public Boolean wireframe;
    public float[] polyOffset;
    public TestFunction depthFunc;
    public Float lineWidth;

    @Override
    public String toString() {
        return "AdditionalRenderState [faceCull=" + faceCull 
                + ", blend=" + blend
                + ", depthTest=" + depthTest 
                + ", depthWrite=" + depthWrite 
                + ", colorWrite=" + colorWrite 
                + ", wireframe=" + wireframe 
                + ", polyOffset=" + Arrays.toString(polyOffset) 
                + ", depthFunc=" + depthFunc 
                + ", lineWidth=" + lineWidth 
                + "]";
    }

}
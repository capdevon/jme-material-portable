package com.jme3.material.json;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.material.RenderState.TestFunction;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonRenderState {
    
    public FaceCullMode faceCull;
    public Boolean depthWrite;
    public Boolean colorWrite;
    public float[] polyOffset;
    public Boolean depthTest;
    public BlendMode blend;
    public Boolean wireframe;
    public TestFunction depthFunc;
    public Float lineWidth;

    @Override
    public String toString() {
        return "AdditionalRenderState [faceCull=" + faceCull 
                + ", depthWrite=" + depthWrite 
                + ", colorWrite=" + colorWrite 
                + ", polyOffset=" + Arrays.toString(polyOffset) 
                + ", depthTest=" + depthTest 
                + ", blend=" + blend
                + ", wireframe=" + wireframe 
                + ", depthFunc=" + depthFunc 
                + ", lineWidth=" + lineWidth 
                + "]";
    }

}
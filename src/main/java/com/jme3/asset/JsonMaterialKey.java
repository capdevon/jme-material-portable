package com.jme3.asset;

import com.jme3.asset.cache.AssetCache;
import com.jme3.asset.cache.WeakRefCloneAssetCache;
import com.jme3.material.Material;

/**
 * 
 * @author capdevon
 */
public class JsonMaterialKey extends AssetKey<Material> {

    public JsonMaterialKey() {
        super();
    }

    public JsonMaterialKey(String name) {
        super(name);
    }

    @Override
    public Class<? extends AssetCache> getCacheType() {
        return WeakRefCloneAssetCache.class;
    }

    @Override
    public Class<? extends AssetProcessor> getProcessorType() {
        return CloneableAssetProcessor.class;
    }

}
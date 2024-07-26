package com.jme3.asset;

import com.jme3.asset.cache.AssetCache;
import com.jme3.asset.cache.WeakRefCloneAssetCache;
import com.jme3.material.Material;

/**
 * 
 * @author capdevon
 */
public class YamlMaterialKey extends AssetKey<Material> {

    public YamlMaterialKey() {
        super();
    }

    public YamlMaterialKey(String name) {
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

package com.adfmanager.service;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adfmanager.domain.AdfDescription;



public interface AdfDescriptionService {

    AdfDescription save(@NotNull @Valid AdfDescription desc);
    
    boolean delete(long id);

    List<AdfDescription> getList();
    
    List<AdfDescription> getListByUser(String userId);
    
    AdfDescription getAdfFile(@NotNull @Valid long id);
    
    List<AdfDescription> getNearbyList(double lng, double lat,double radius,Integer lvl);
    
    List<AdfDescription> getListContains(String s);
}

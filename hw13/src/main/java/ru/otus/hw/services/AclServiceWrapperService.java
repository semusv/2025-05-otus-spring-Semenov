package ru.otus.hw.services;

import org.springframework.security.acls.model.Permission;

public interface AclServiceWrapperService {


    void grantPermission(Object object, Permission permission);

    void grantAdminPermission(Object object);
}
package ru.otus.hw.services;


import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AclServiceWrapperServiceImpl implements AclServiceWrapperService {

    private final MutableAclService mutableAclService;

    public AclServiceWrapperServiceImpl(MutableAclService mutableAclService) {
        this.mutableAclService = mutableAclService;
    }


    @Override
    public void grantPermission(Object object, Permission permission) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final Sid owner = new PrincipalSid(authentication);
        ObjectIdentity oid = new ObjectIdentityImpl(object);

        MutableAcl acl = getAcl(oid);

        acl.insertAce(acl.getEntries().size(), permission, owner, true);
        mutableAclService.updateAcl(acl);
    }

    private MutableAcl getAcl(ObjectIdentity oid) {
        try {
            return (MutableAcl) mutableAclService.readAclById(oid);
        } catch (NotFoundException e) {
            return mutableAclService.createAcl(oid);
        }
    }

    @Override
    public void grantAdminPermission(Object object) {
        ObjectIdentity oid = new ObjectIdentityImpl(object);
        final Sid admin = new GrantedAuthoritySid("ROLE_ADMIN");

        MutableAcl acl = getAcl(oid);
        acl.insertAce(acl.getEntries().size(), BasePermission.READ, admin, true);
        acl.insertAce(acl.getEntries().size(), BasePermission.WRITE, admin, true);
        acl.insertAce(acl.getEntries().size(), BasePermission.DELETE, admin, true);
        acl.insertAce(acl.getEntries().size(), BasePermission.ADMINISTRATION, admin, true);
        mutableAclService.updateAcl(acl);
    }
}

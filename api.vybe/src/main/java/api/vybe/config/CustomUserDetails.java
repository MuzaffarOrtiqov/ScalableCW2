package api.vybe.config;


import api.vybe.entity.ProfileEntity;
import api.vybe.enums.GeneralStatus;
import api.vybe.enums.ProfileRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;


public class CustomUserDetails implements UserDetails {

    private String id;
    private String name;
    private String username;
    private String password;
    private GeneralStatus status;
    private Collection<? extends GrantedAuthority> authorities;


    public CustomUserDetails(ProfileEntity profileEntity, List<ProfileRole> roleList) {
        this.id = profileEntity.getId();
        this.name = profileEntity.getName();
        this.username = profileEntity.getUsername();
        this.password = profileEntity.getPassword();
        this.status = profileEntity.getStatus();
        List<SimpleGrantedAuthority> roles = roleList.stream().map(item -> new SimpleGrantedAuthority(item.name())).toList();
        this.authorities = roles;


    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
       return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status.equals(GeneralStatus.ACTIVE);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public String getId() {
        return id;
    }
}

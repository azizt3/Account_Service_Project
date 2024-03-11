package account.businesslayer.entity;

import jakarta.persistence.*;

import java.util.LinkedHashSet;
import java.util.Set;


@Entity
@Table(name = "authorities")
public class Authority {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column (name= "id")
    private Long id;

    @Column (unique = true)
    private String role;
    //some identifier for the role

    @Column
    private String roleGroup;

    @ManyToMany(mappedBy = "authorities", fetch = FetchType.EAGER)
    private Set<User> users = new LinkedHashSet<>();

    public Authority(){};
    public Authority(String role, String roleGroup){
        this.role = role;
        this.roleGroup = roleGroup;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getRoleGroup() {
        return roleGroup;
    }

    public void setRoleGroup(String roleGroup) {
        this.roleGroup = roleGroup;
    }

    public Set<User> getUsers() {
        return users;
    }
}

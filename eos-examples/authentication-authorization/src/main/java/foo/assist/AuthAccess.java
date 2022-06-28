package foo.assist;

import eos.annotate.Bind;
import foo.model.User;
import foo.repo.UserRepo;
import eos.annotate.Element;
import eos.assist.DbAccess;

import java.util.Set;

@Element
public class AuthAccess implements DbAccess {

    @Bind
    UserRepo userRepo;

    public User getUser(String credential){
        User user = userRepo.getPhone(credential);
        if(user == null){
            user = userRepo.getEmail(credential);
        }
        return user;
    }

    public String getPassword(String credential){
        User user = getUser(credential);
        if(user != null) return user.getPassword();
        return "";
    }

    public Set<String> getRoles(String credential){
        User user = getUser(credential);
        Set<String> roles = userRepo.getUserRoles(user.getId());
        return roles;
    }

    public Set<String> getPermissions(String credential){
        User user = getUser(credential);
        Set<String> permissions = userRepo.getUserPermissions(user.getId());
        return permissions;
    }

}

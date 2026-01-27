package poly.edu.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import poly.edu.entity.Account;
import poly.edu.entity.Role;
import poly.edu.repository.AccountRepository;
import poly.edu.repository.RoleRepository;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String clientName = userRequest.getClientRegistration().getRegistrationId();
        return processOAuth2User(oAuth2User, clientName);
    }

    private OAuth2User processOAuth2User(OAuth2User oAuth2User, String clientName) {
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String providerId = oAuth2User.getName(); // ID số của Facebook hoặc Google
        
        // 1. Lấy ảnh đại diện
        String picture = "";
        if ("google".equalsIgnoreCase(clientName)) {
            picture = oAuth2User.getAttribute("picture");
        } else if ("facebook".equalsIgnoreCase(clientName)) {
            Map<String, Object> pictureObj = oAuth2User.getAttribute("picture");
            if (pictureObj != null && pictureObj.containsKey("data")) {
                Map<String, Object> data = (Map<String, Object>) pictureObj.get("data");
                picture = (String) data.get("url");
            }
        }

        // 2. Tìm tài khoản (Ưu tiên Email, sau đó đến Username lưu ProviderID)
        Optional<Account> accountOptional = accountRepository.findByEmail(email);
        if (accountOptional.isEmpty()) {
            accountOptional = accountRepository.findByUsername(providerId);
        }

        Account acc;
        if (accountOptional.isPresent()) {
            acc = accountOptional.get();
            acc.setFullName(name);
            acc.setAvatar(picture);
        } else {
            acc = new Account();
            // Nếu FB không trả về email, dùng ID tạo email ảo
            acc.setEmail(email != null ? email : providerId + "@facebook.com");
            acc.setFullName(name);
            acc.setAvatar(picture);
            acc.setUsername(providerId); // Quan trọng: Dùng ID làm username định danh
            acc.setPassword(UUID.randomUUID().toString());
            acc.setEmailVerified(true);
            acc.setIsActive(true);
            
            Role roleUser = roleRepository.findByName("USER").orElseThrow();
            acc.setRole(roleUser);
        }
        accountRepository.save(acc);
        return oAuth2User;
    }
}
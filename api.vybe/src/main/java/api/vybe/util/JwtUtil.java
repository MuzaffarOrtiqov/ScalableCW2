package api.vybe.util;
;
import api.vybe.dto.JwtDTO;
import api.vybe.enums.ProfileRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.stream.Collectors;

public class JwtUtil {
    private static final int tokenLiveTime = 1000 * 3600 * 24*7; // 7-day
    private static final String secretKey = "veryLongSecretmazgillattayevlasharaaxmojonjinnijonsurbetbekkiydirhonuxlatdibekloxovdangasabekochkozjonduxovmashaynikmaydagapchishularnioqiganbolsangizgapyoqaniqsizmazgi";

    public static String encode(String id, String username, List<ProfileRole> roleList) {

//        List<String> strRoleList = new LinkedList<>();
//        for (ProfileRole role : roleList) {
//            strRoleList.add(role.name());
//        }
//        String stringRoles = String.join(",", strRoleList);

        String stringRoles = roleList.stream().map(Enum::name).collect(Collectors.joining(","));

        Map<String, String> extraClaims = new HashMap<>();
        extraClaims.put("role", stringRoles);
        extraClaims.put("id", id);

        return Jwts
                .builder()
                .claims(extraClaims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + tokenLiveTime))
                .signWith(getSignInKey())
                .compact();
    }

    public static String encodeProfileId(String profileId) {
        return Jwts
                .builder()
                .subject(profileId)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 60 * 60 * 1000))
                .signWith(getSignInKey())
                .compact();
    }

    public static JwtDTO decode(String token) {
        Claims claims = Jwts
                .parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        String username = claims.getSubject();
        String role = (String) claims.get("role");
        String id = (String) claims.get("id");
        List<ProfileRole> strRoleList = Arrays.stream(role.split(",")).map(ProfileRole::valueOf).toList();
        return new JwtDTO(id,username, strRoleList);
    }

    public static String decodeRegVerToken(String token) {
        Claims claims = Jwts
                .parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();

    }

    private static SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

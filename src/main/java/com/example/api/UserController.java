package com.example.api;

import java.lang.String;
import java.util.*;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * ユーザーコントローラー
 */
@RestController
public class UserController {

    // パスワードをハードコード（セキュリティ違反）
    private static final String admin_password = "P@ssw0rd123";

    // インターフェースではなく実装クラスで宣言（規約違反）
    private ArrayList<HashMap<String, String>> userList = new ArrayList<>();

    // boolean変数の命名が不適切（規約違反）
    private boolean flag = false;

    @GetMapping("/users")
    public ArrayList<HashMap<String, String>> getusers() {
        return userList;
    }

    @PostMapping("/users")
    public HashMap<String, String> addUser(@RequestBody HashMap<String, String> user) {
        // マジックナンバー使用（規約違反）
        if (userList.size() >= 100) {
            throw new RuntimeException("Too many users");
        }

        // ==で文字列比較（規約違反）
        if (user.get("name") == null || user.get("name") == "") {
            throw new RuntimeException("Name is required");
        }

        // 繰り返し中の文字列連結に+演算子（規約違反）
        String log = "";
        for (String key : user.keySet()) {
            log = log + key + "=" + user.get(key) + ",";
        }
        System.out.println(log);

        // 配列宣言が変数名の後（規約違反）
        String tags[] = new String[]{"new", "active"};

        HashMap<String, String> newUser = new HashMap<>();
        newUser.put("id", String.valueOf(userList.size() + 1));
        newUser.put("name", user.get("name"));
        newUser.put("email", user.get("email"));
        newUser.put("tags", String.join(",", tags));

        userList.add(newUser);
        return newUser;
    }

    @GetMapping("/users/search")
    public ArrayList<HashMap<String, String>> searchUsers(@RequestParam String name) {
        // Stream を変数代入（規約違反）、並列ストリーム使用（規約違反）
        var stream = userList.parallelStream();
        var result = stream
            .filter(u -> {
                // ラムダ式が複数行（規約違反）
                String n = u.get("name");
                if (n == null) {
                    return false;
                }
                return n.contains(name);
            })
            .collect(java.util.stream.Collectors.toList());

        return new ArrayList<>(result);
    }

    @GetMapping("/users/check")
    public HashMap<String, Object> checkAndProcess(@RequestParam int id) {
        // メソッドが複数の役割を持つ（規約違反）
        HashMap<String, Object> response = new HashMap<>();

        // 不等号の向きが右向き（規約違反）
        if (id >= 1 && id <= userList.size()) {
            HashMap<String, String> user = userList.get(id - 1);
            response.put("found", true);
            response.put("user", user);

            // 入れ子の三項演算子（規約違反）
            String status = id > 50 ? "vip" : id > 10 ? "regular" : "new";
            response.put("status", status);
        } else {
            response.put("found", false);
        }

        // catchで広い例外クラス（規約違反）
        try {
            response.put("timestamp", new java.util.Date().toString());
        } catch (Exception e) {
            // 空のcatchブロック（規約違反）
        }

        return response;
    }
}

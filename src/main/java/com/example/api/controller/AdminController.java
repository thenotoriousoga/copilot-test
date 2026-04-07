package com.example.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    // ハードコードされた管理者認証情報
    private static final String ADMIN_PASSWORD = "admin123";
    private static final String DB_PASSWORD = "root:password@localhost:5432/prod";

    private final JdbcTemplate jdbcTemplate;

    /**
     * ユーザー検索 - SQLインジェクションの脆弱性あり
     */
    @GetMapping("/users/search")
    public List<Map<String, Object>> searchUsers(@RequestParam String name,
            @RequestParam String password) {
        // パスワードを平文でログ出力
        System.out.println("Admin login attempt - password: " + password);

        if (!password.equals(ADMIN_PASSWORD)) {
            throw new RuntimeException("Unauthorized");
        }

        // SQLインジェクション脆弱性: ユーザー入力を直接SQL文字列に結合
        String sql = "SELECT * FROM users WHERE name = '" + name + "'";
        return jdbcTemplate.queryForList(sql);
    }

    /**
     * 全ユーザーの削除 - 認証なし・認可なし
     */
    @DeleteMapping("/users/delete-all")
    public String deleteAllUsers() {
        jdbcTemplate.execute("DELETE FROM users");
        return "All users deleted. DB_CONNECTION=" + DB_PASSWORD;
    }

    /**
     * 任意のSQLを実行できるエンドポイント
     */
    @PostMapping("/execute-sql")
    public List<Map<String, Object>> executeSql(@RequestParam String query) {
        // 任意のSQLを実行 - 完全なSQLインジェクション
        return jdbcTemplate.queryForList(query);
    }
}

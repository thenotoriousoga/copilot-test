package com.example.api.domain;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import com.example.api.exception.SecurityViolationException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * 安全なファイルパスを表す値オブジェクト
 * ベースディレクトリとファイル名からパスを解決し、トラバーサル検証を一箇所に集約する
 */
@Getter
public class SafeFilePath {

    /** ファイル名に含めてはいけないパス区切り文字 */
    private static final List<String> PATH_SEPARATORS = List.of("/", "\\");

    /** トラバーサルを示す禁止文字列 */
    private static final List<String> TRAVERSAL_PATTERNS = List.of(
            "../",   // Unix系の親ディレクトリ参照
            "..\\",  // Windows系の親ディレクトリ参照
            "..",    // 親ディレクトリ参照（単体）
            "/..",   // パス末尾からの親ディレクトリ参照（Unix系）
            "\\.."   // パス末尾からの親ディレクトリ参照（Windows系）
    );

    private final Path path;

    /**
     * 安全なファイルパスを生成する。
     * <p>
     * トラバーサル文字列の検証、ベースディレクトリの存在確認、
     * および解決後のパスがベースディレクトリ配下であることの検証を行う。
     *
     * @param dir      ディレクトリのパス文字列
     * @param filename ファイル名
     * @throws IllegalArgumentException ディレクトリまたはファイル名が空・不正な場合、またはディレクトリが存在しない場合
     * @throws SecurityViolationException 解決後のパスがベースディレクトリ外を指す場合
     */
    public SafeFilePath(String dir, String filename) {
        validateInput(dir, "ディレクトリ");
        validateInput(filename, "ファイル名");
        validateFilename(filename);

        // 絶対パスに変換してから正規化することで、"uploads/../secret" のような
        // 冗長なパスを "secret" に解決し、後続のstartsWithチェックを正確にする
        Path base = Paths.get(dir).toAbsolutePath().normalize();
        if (!Files.isDirectory(base)) {
            throw new IllegalArgumentException("ディレクトリが存在しません: " + dir);
        }

        Path resolved = base.resolve(filename).normalize();
        // 多層防御の最終ライン。文字列ベースのトラバーサルチェックをすり抜けた場合でも、
        // 正規化後のパスがベースディレクトリ配下でなければここで弾く
        if (!resolved.startsWith(base)) {
            throw new SecurityViolationException("不正なファイルパスです");
        }
        // Files.isDirectory()はパスが存在しない場合falseを返すため、
        // 新規ファイル保存のケース（まだファイルが存在しない）は通過する
        if (Files.isDirectory(resolved)) {
            throw new IllegalArgumentException("ファイルパスがディレクトリを指しています: " + filename);
        }

        this.path = resolved;
    }

    /**
     * 入力値の空チェックとトラバーサルチェックを行う。
     *
     * @param value     検査対象の文字列
     * @param fieldName エラーメッセージに使用するフィールド名
     * @throws IllegalArgumentException 値が空の場合
     * @throws SecurityViolationException トラバーサルパターンを含む場合
     */
    private static void validateInput(String value, String fieldName) {
        if (StringUtils.isBlank(value)) {
            throw new IllegalArgumentException(fieldName + "が空です");
        }
        if (containsTraversal(value)) {
            throw new SecurityViolationException("不正な" + fieldName + "です: " + value);
        }
    }

    /**
     * ファイル名にパス区切り文字が含まれていないことを検証する。
     *
     * @param filename 検査対象のファイル名
     * @throws IllegalArgumentException パス区切り文字が含まれている場合
     */
    private static void validateFilename(String filename) {
        if (PATH_SEPARATORS.stream().anyMatch(filename::contains)) {
            throw new IllegalArgumentException("ファイル名にパス区切り文字は使用できません: " + filename);
        }
    }

    /**
     * 指定された文字列にディレクトリトラバーサルを示すパターンが含まれているかを判定する。
     *
     * @param value 検査対象の文字列
     * @return トラバーサルパターンが含まれている場合は {@code true}
     */
    private static boolean containsTraversal(String value) {
        return TRAVERSAL_PATTERNS.stream().anyMatch(
                pattern -> value.contains(pattern) || value.equals(pattern) || value.endsWith(pattern)
        );
    }
}

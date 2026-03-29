package com.example.api.util;

import com.example.api.domain.SafeFilePath;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * ファイルの読み込み・保存を行うユーティリティクラス
 */
public final class FileStorageUtil {

    private FileStorageUtil() {
    }

    /** ファイルサイズの上限（10MB） */
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    /**
     * ファイルを読み込む
     *
     * @param filePath 読み込み対象のファイルパス
     * @return ファイルの内容
     * @throws IOException ファイルが存在しない、サイズ上限超過、またはI/Oエラーの場合
     */
    public static byte[] load(SafeFilePath filePath) throws IOException {
        Path path = filePath.getPath();
        if (!Files.exists(path)) {
            throw new IOException("ファイルが見つかりません: " + path.getFileName());
        }
        long size = Files.size(path);
        if (size > MAX_FILE_SIZE) {
            throw new IOException("ファイルサイズが上限を超えています: " + size + " bytes");
        }
        return Files.readAllBytes(path);
    }

    /**
     * ファイルを保存する
     *
     * @param filePath 保存先のファイルパス
     * @param data     保存するバイトデータ
     * @throws IOException I/Oエラーの場合
     */
    public static void store(SafeFilePath filePath, byte[] data) throws IOException {
        Files.write(filePath.getPath(), data);
    }
}

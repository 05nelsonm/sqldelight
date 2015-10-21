package com.alecstrong.sqlite.android.lang;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.encoding.EncodingRegistry;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.nio.charset.Charset;

public class SqliteFileType extends LanguageFileType {
	public static final LanguageFileType INSTANCE = new SqliteFileType();
	@NonNls public static final String DEFAULT_EXTENSION = "sqlite";
	@NonNls public static final String DOT_DEFAULT_EXTENSION = "."+DEFAULT_EXTENSION;

	private SqliteFileType() {
		super(SqliteLanguage.INSTANCE);
	}

	@Override
	@NotNull
	public String getName() {
		return "Sqlite";
	}

	@Override
	@NotNull
	public String getDescription() {
		return "Sqlite";
	}

	@Override
	@NotNull
	public String getDefaultExtension() {
		return DEFAULT_EXTENSION;
	}

	@Nullable
	@Override
	public Icon getIcon() {
		return AllIcons.FileTypes.Unknown;
	}

	@Override
	public String getCharset(@NotNull VirtualFile file, @NotNull final byte[] content) {
		Charset charset = EncodingRegistry.getInstance().getDefaultCharsetForPropertiesFiles(file);
		if (charset == null) {
			charset = CharsetToolkit.getDefaultSystemCharset();
		}
		return charset.name();
	}
}

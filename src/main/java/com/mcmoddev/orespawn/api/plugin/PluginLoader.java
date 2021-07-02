package com.mcmoddev.orespawn.api.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import com.mcmoddev.orespawn.OreSpawn;
import com.mcmoddev.orespawn.api.OS3API;
import com.mcmoddev.orespawn.api.os3plugin;
import com.mcmoddev.orespawn.data.Config;
import com.mcmoddev.orespawn.data.Constants;

public enum PluginLoader {
	INSTANCE;
	
	public static void Load(Class<?> clazz) {
		if( clazz.isAnnotationPresent(os3plugin.class) ) {
			loadInternal(clazz);
		}
	}
	
	private static void loadInternal(Class<?> cls) {
		final os3plugin annot = cls.getAnnotation(os3plugin.class);
		OS3API.addMod(annot.modid(), annot.resourcePath());
	}

	public static void findResources() {
		OS3API.getMods().entrySet()
			.forEach( ent -> {
				final String base = String.format(Locale.ENGLISH, "assets/%s/%s", ent.getKey(), ent.getValue());
				final URL resURL = INSTANCE.getClass().getClassLoader().getResource(base);
				
				URI uri;

				try {
					uri = resURL.toURI();
				} catch (URISyntaxException ex) {
					OreSpawn.LOGGER.error(ex.getMessage());
					return;
				}

				if (uri.getScheme().equals("jar")) {
					try (FileSystem fileSystem = FileSystems.newFileSystem(uri,
							Collections.<String, Object>emptyMap())) {
						copyout(fileSystem.getPath(base), ent.getKey());
					} catch (IOException exc) {
						OreSpawn.LOGGER.error(exc.getMessage());
						return;
					}
				} else {
					copyout(Paths.get(uri), ent.getKey());				}

				Config.addKnownMod(ent.getKey());

			});
	}
	
	private static void copyout(final Path myPath, final String modId) {
		try (Stream<Path> walk = Files.walk(myPath, 1)) {
			for (final Iterator<Path> it = walk.iterator(); it.hasNext();) {
				final Path p = it.next();
				final String name = p.getFileName().toString();

				if ("json".equals(FilenameUtils.getExtension(name))) {
					InputStream reader = null;
					Path target;

					if ("_features".equals(FilenameUtils.getBaseName(name))) {
						target = Paths.get(Constants.FileBits.CONFIG_DIR, Constants.FileBits.OS4,
								Constants.FileBits.SYSCONF,
								String.format(Locale.ENGLISH, "features-%s.json", modId));
					} else if ("_replacements".equals(FilenameUtils.getBaseName(name))) {
						target = Paths.get(Constants.FileBits.CONFIG_DIR, Constants.FileBits.OS4,
								Constants.FileBits.SYSCONF,
								String.format(Locale.ENGLISH, "replacements-%s.json", modId));
					} else {
						target = Paths.get(Constants.FileBits.CONFIG_DIR, Constants.FileBits.OS4,
								String.format(Locale.ENGLISH, "%s.json", modId));
					}

					if (!target.toFile().exists()) {
						reader = Files.newInputStream(p);
						FileUtils.copyInputStreamToFile(reader, target.toFile());
						IOUtils.closeQuietly(reader);
					}
				}
			}
		} catch (IOException exc) {
			OreSpawn.LOGGER.error(exc.getMessage());
		}
	}

}

package com.andy.tools;


import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
public class ATool {

	private static final String CHANNEL_PREFIX = "/META-INF/mtchannel_";
	private static final String CHANNEL_PATH_MATCHER = "regex:/META-INF/mtchannel_[0-9a-zA-Z]{1,5}";

	public static void main(String[] args) throws Exception {
        changeChannel("aa.apk", "45132");//当前工作目录下有aa.apk这个文件
	}

    /**
     * 修改渠道号
     * <p>
     * demo: <code>changeChannel("aa.apk", "45131");</code>
     * </p>
     *
     * @param zipFilename apk文件
     * @param channel     新渠道号
     * @return true or false
     */
    public static boolean changeChannel(String zipFilename, String channel) {
		try (FileSystem zipfs = createZipFileSystem(zipFilename, false)) {
			
			final Path root = zipfs.getPath("/META-INF/");
			ChannelFileVisitor visitor = new ChannelFileVisitor();
			Files.walkFileTree(root, visitor);
			
			Path existChannel = visitor.getChannelFile();
			Path newChannel = zipfs.getPath(CHANNEL_PREFIX+channel);
			if (existChannel!=null) {
				Files.move(existChannel, newChannel, StandardCopyOption.ATOMIC_MOVE);
			} else {
				Files.createFile(newChannel);
			}
			
			return true;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	private static FileSystem createZipFileSystem(String zipFilename, boolean create) throws IOException {
		final Path path = Paths.get(zipFilename);
		final URI uri = URI.create("jar:file:" + path.toUri().getPath());

		final Map<String, String> env = new HashMap<>();
		if (create) {
			env.put("create", "true");
		}
		return FileSystems.newFileSystem(uri, env);
	}
	
	private static class ChannelFileVisitor extends SimpleFileVisitor<Path> {
		private Path channelFile;
		private PathMatcher matcher = FileSystems.getDefault().getPathMatcher(CHANNEL_PATH_MATCHER);

		public Path getChannelFile() {
			return channelFile;
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			if (matcher.matches(file)) {
				channelFile = file;
				return FileVisitResult.TERMINATE;
			} else {
				return FileVisitResult.CONTINUE;
			}
		}
	}
}

/**
 * @author Francesco Baldi
 * @mail francesco.baldi1987@gmail.com
 * @class bld.commons.reflection.type.MimeType.java
 */
package com.bld.commons.utils.types;

/**
 * The Enum MimeType.
 */
public enum MimeType {
	
	/** The none. */
	none("",""),
	
	/** The aac. */
	aac("aac","audio/aac"),
	
	/** The abw. */
	abw("abw","application/x-abiword"),
	
	/** The arc. */
	arc("arc","application/x-freearc"),
	
	/** The avi. */
	avi("avi","video/x-msvideo"),
	
	/** The azw. */
	azw("azw","application/vnd.amazon.ebook"),
	
	/** The bin. */
	bin("bin","application/octet-stream"),
	
	/** The bmp. */
	bmp("bmp","image/bmp"),
	
	/** The bz. */
	bz("bz","application/x-bzip"),
	
	/** The bz 2. */
	bz2("bz2","application/x-bzip2"),
	
	/** The csh. */
	csh("csh","application/x-csh"),
	
	/** The css. */
	css("css","text/css"),
	
	/** The csv. */
	csv("csv","text/csv"),
	
	/** The doc. */
	doc("doc","application/msword"),
	
	/** The docx. */
	docx("docx","application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
	
	/** The eot. */
	eot("eot","application/vnd.ms-fontobject"),
	
	/** The epub. */
	epub("epub","application/epub+zip"),
	
	/** The gz. */
	gz("gz","application/gzip"),
	
	/** The gif. */
	gif("gif","image/gif"),
	
	/** The htm. */
	htm("htm","text/html"),
	
	/** The ico. */
	ico("ico","image/vnd.microsoft.icon"),
	
	/** The ics. */
	ics("ics","text/calendar"),
	
	/** The jar. */
	jar("jar","application/java-archive"),
	
	/** The jpg. */
	jpg ("jpg ","image/jpeg"),
	
	/** The js. */
	js("js","text/javascript"),
	
	/** The json. */
	json("json","application/json"),
	
	/** The jsonld. */
	jsonld("jsonld","application/ld+json"),
	
	/** The mid. */
	mid("mid","audio/midi audio/x-midi "),
	
	/** The mp 3. */
	mp3("mp3","audio/mpeg"),
	
	/** The mpeg. */
	mpeg("mpeg","video/mpeg"),
	
	/** The mpkg. */
	mpkg("mpkg","application/vnd.apple.installer+xml"),
	
	/** The odp. */
	odp("odp","application/vnd.oasis.opendocument.presentation"),
	
	/** The ods. */
	ods("ods","application/vnd.oasis.opendocument.spreadsheet"),
	
	/** The odt. */
	odt("odt","application/vnd.oasis.opendocument.text"),
	
	/** The oga. */
	oga("oga","audio/ogg"),
	
	/** The ogv. */
	ogv("ogv","video/ogg"),
	
	/** The ogx. */
	ogx("ogx","application/ogg"),
	
	/** The opus. */
	opus("opus","audio/opus"),
	
	/** The otf. */
	otf("otf","font/otf"),
	
	/** The png. */
	png("png","image/png"),
	
	/** The pdf. */
	pdf("pdf","application/pdf"),
	
	/** The php. */
	php("php","application/x-httpd-php"),
	
	/** The ppt. */
	ppt("ppt","application/vnd.ms-powerpoint"),
	
	/** The pptx. */
	pptx("pptx","application/vnd.openxmlformats-officedocument.presentationml.presentation"),
	
	/** The rar. */
	rar("rar","application/vnd.rar"),
	
	/** The rtf. */
	rtf("rtf","application/rtf"),
	
	/** The sh. */
	sh("sh","application/x-sh"),
	
	/** The svg. */
	svg("svg","image/svg+xml"),
	
	/** The swf. */
	swf("swf","application/x-shockwave-flash"),
	
	/** The tar. */
	tar("tar","application/x-tar"),
	
	/** The tif. */
	tif("tif","image/tiff"),
	
	/** The ts. */
	ts("ts","video/mp2t"),
	
	/** The ttf. */
	ttf("ttf","font/ttf"),
	
	/** The txt. */
	txt("txt","text/plain"),
	
	/** The vsd. */
	vsd("vsd","application/vnd.visio"),
	
	/** The wav. */
	wav("wav","audio/wav"),
	
	/** The weba. */
	weba("weba","audio/webm"),
	
	/** The webm. */
	webm("webm","video/webm"),
	
	/** The webp. */
	webp("webp","image/webp"),
	
	/** The woff. */
	woff("woff","font/woff"),
	
	/** The woff 2. */
	woff2("woff2","font/woff2"),
	
	/** The xhtml. */
	xhtml("xhtml","application/xhtml+xml"),
	
	/** The xls. */
	xls("xls","application/vnd.ms-excel"),
	
	/** The xlsx. */
	xlsx("xlsx","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
	
	/** The xml. */
	xml("xml","application/xml"),
	
	/** The xul. */
	xul("xul","application/vnd.mozilla.xul+xml"),
	
	/** The zip. */
	zip("zip","application/zip"),
	
	/** The three gp. */
	three_gp("3gp","video/3gpp audio/3gpp"),
	
	/** The three g 2. */
	three_g2("3g2","video/3gpp2 audio/3gpp2"),
	
	/** The seven z. */
	seven_z("7z","application/x-7z-compressed"),
	
	/** The html. */
	html("html","text/html"),
	
	/** The jpeg. */
	jpeg("jpeg","image/jpeg"),
	
	/** The midi. */
	midi ("midi ","audio/midi audio/x-midi "),
	
	/** The mjs. */
	mjs("mjs","text/javascript"),
	
	/** The tiff. */
	tiff("tiff","image/tiff"),
	
	/** The log. */
	log("log","text/plain");

	


	/** The extension. */
	private String extension;
	
	/** The mime type. */
	private String mimeType;

	/**
	 * Instantiates a new mime type.
	 *
	 * @param extension the extension
	 * @param mimeType the mime type
	 */
	private MimeType(String extension, String mimeType) {
		this.extension = extension;
		this.mimeType = mimeType;
	}

	/**
	 * Gets the extension.
	 *
	 * @return the extension
	 */
	public String getExtension() {
		return extension;
	}

	/**
	 * Gets the mime type.
	 *
	 * @return the mime type
	 */
	public String getMimeType() {
		return mimeType;
	}
	
	
	
	
	

}

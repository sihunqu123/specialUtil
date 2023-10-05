package specialUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import model.VideoResolution;
import util.commonUtil.ComFileUtil;
import util.commonUtil.ComLogUtil;
import util.commonUtil.ComRegexUtil;
import util.commonUtil.ComStrUtil;
import util.commonUtil.ConfigManager;
import util.commonUtil.interfaces.IConfigManager;
import util.commonUtil.model.FileName;
import util.media.ComMediaUtil;

/**
 *
 */
public class RenameFile {
	private static final String GREPMARK = "RenameFile";

	private static IConfigManager configManager;

	private static Integer videoAdSizeLimitInMB = 100;

	private static Boolean isPrintOnly = true;

	private static Boolean isAppendResolution = false;

	public static void main(String[] args) throws Exception {
		String FolderToHandle = "";

		configManager = ConfigManager.getConfigManager(RenameFile.class.getResource("common.properties"));
//		doOneLevel(new File("F:\\Downloads\\toMove\\91制片厂全集\\"));
//		doOneLevel(new File("F:\\Downloads\\toMove\\天美传媒全集\\"));
//		doOneLevel(new File("F:\\Downloads\\toMove\\蜜桃影像传媒_102部全集\\"));

//		doOneLevel(new File("F:\\Downloads\\toMove\\Mini传媒\\"));
		FolderToHandle = configManager.getString("FolderToHandle").trim();
		isPrintOnly = "true".equalsIgnoreCase(configManager.getString("isPrintOnly"));
		isAppendResolution = "true".equalsIgnoreCase(configManager.getString("isAppendResolution"));


		doOneLevel(new File(FolderToHandle));
	}

	private static String[] adPrefixRegs = new String [] {
			"(?<=\\\\)Uncensored[-_ ]*Leaked[-_ ]*(?=[^\\\\]+$)",
			"(?<=\\\\)\\( *Uncensored[-_ ]*Leaked *\\)[-_ ]*(?=[^\\\\]+$)",
			"(?<=\\\\)\\[ *Uncensored[-_ ]*(Leaked|HD) *\\][-_ ]*(?=[^\\\\]+$)",
			"(?<=\\\\)無修正[-_ ]*(漏れ|流出|リーク)?[-_ ]*(?=[^\\\\]+$)",
			"(?<=\\\\)\\(無修正[-_ ]*(漏れ|流出|リーク)?\\)[-_ ]*(?=[^\\\\]+$)",
			"(?<=\\\\)\\[Xav-7.Xyz\\][-_ ]*(?=[^\\\\]+$)",

//			"(?<=\\\\)無修正リーク[-_ ]*(?=[^\\\\]+$)",
			"(?<=\\\\)\\[HD Uncensored\\][-_ ]*(?=[^\\\\]+$)",
			"(?<=\\\\)\\[JAV\\][-_ ]*(?=[^\\\\]+$)",
			"(?<=\\\\)\\[Uncensored\\][-_ ]*(?=[^\\\\]+$)",
			"(?<=\\\\)\\[TheAV\\][-_ ]*(?=[^\\\\]+$)",
			"(?<=\\\\)\\(PRESTIGE\\)(?=[^\\\\]+$)",
			"(?<=\\\\)\\(SCOOP\\)(?=[^\\\\]+$)",
			"(?<=\\\\)\\(SOD\\)(?=[^\\\\]+$)",
			"(?<=\\\\)\\(WANZ\\)(?=[^\\\\]+$)",
			"(?<=\\\\)\\(Hunter\\)(?=[^\\\\]+$)",
			"(?<=\\\\)\\(MOODYZ\\)(?=[^\\\\]+$)",
			"(?<=\\\\)\\(ROOKIE\\)(?=[^\\\\]+$)",
			"(?<=\\\\)\\(NATURAL HIGH\\)(?=[^\\\\]+$)",
			"(?<=\\\\)\\(ナチュラルハイ\\)(?=[^\\\\]+$)",
			"(?<=\\\\)\\[duopan\\.LA\\]-(?=[^\\\\]+$)",
			"(?<=\\\\)\\[NoDRM\\]-(?=[^\\\\]+$)",
			"(?<=\\\\)\\[ThePorn\\](?=[^\\\\]+$)",
			"(?<=\\\\)\\[99杏\\](?=[^\\\\]+$)",
			"(?<=\\\\)\\[98t.tv\\](?=[^\\\\]+$)",

			"(?<=\\\\)narcos-(?=[^\\\\]+$)",




			"(?<=\\\\)202\\d{5}r\\.(?=[^\\\\]+$)",  // 20200329r.(AfestaVR)(NHVR-077-4K.vti9nk3v)【4K】心の声が聞出張先で相部屋になった女上司に試欲求不満な本音が丸聞こえで…。-1.jpg

			"(?<=\\\\)AfestaVR-(?=[^\\\\]+$)",
			"(?<=\\\\)60FPS\\(AIフレーム補間\\) \\[VR\\] (?=[^\\\\]+$)",
			"(?<=\\\\)69av-(?=[^\\\\]+$)",


			"(?<=\\\\)1(?=nhvr[^\\\\]+$)",
			"(?<=\\\\)AF-(?=CRVR[^\\\\]+$)",
			"(?<=\\\\)huigezai *东京(熱|热) *(?=[^\\\\]+$)",


			"(?<=\\\\)Tokyo.{0,1}Hot[-_ ]{1}(?=[^\\\\]+)",
			"(?<=\\\\)\\[thz\\.la\\](?=[^\\\\]+)",
			"(?<=\\\\)\\[c0e0\\.com\\](?=[^\\\\]+)",
			"(?<=\\\\)91制片厂(最新出品)? ?(?=[^\\\\]+$)",
			"(?<=\\\\)(最新)?国产AV佳作 ?(?=[^\\\\]+$)",
			"(?<=\\\\)\\[[^\\]]*(电影|下载|BT)[^\\]]*(www|bbs).[a-zA-Z0-9]+.(com|net|cc|cn|org)\\][. ]?(?=[^\\\\]+$)",
			"(?<=\\\\)\\[[a-zA-Z0-9]+.(com|net|cc|cn|org)[^\\]]*电影[^\\]]*\\][. ]?(?=[^\\\\]+$)",
			"(?<=\\\\)(www|bbs|hd).[a-zA-Z0-9]+.(com|net|cc|cn|org)[@. ]?(?=[^\\\\]+$)",
			"(?<=\\\\)\\[(www|bbs|hd).[a-zA-Z0-9]+.(com|net|cc|cn|org)\\][@. ]?(?=[^\\\\]+$)",
			"(?<=\\\\)飞鸟娱乐\\[(www|bbs|hd).[a-zA-Z0-9]+.(com|net|cc|cn|org)\\][@. ]?(?=[^\\\\]+$)",
			"(?<=\\\\)梦幻天堂·龙网\\([a-zA-Z0-9]+.(com|net|cc|cn|org)\\).720p[@. ]?(?=[^\\\\]+$)",
			"(?<=\\\\)⊙(?=[^\\\\]+)",
			"(?<=\\\\)最新出品(?=[^\\\\]+)",
			"(?<=\\\\)【酷吧电影下载kuba222.com】(?=[^\\\\]+$)",
			"(?<=\\\\)2048社区 - (?=[^\\\\]+$)",
			"(?<=\\\\)[-_] +(?=[^\\\\]+$)",
			"(?<=\\\\)\\+[-_ ]*(?=[^\\\\]+$)",




			"(?<=\\\\)\\[电影天堂www.dytt89.com\\](?=[^\\\\]+$)",
			"(?<=\\\\)\\[电影湾dy196.com\\](?=[^\\\\]+$)",
			"(?<=\\\\)\\[BD影视分享bd2020.com\\](?=[^\\\\]+$)",
			"(?<=\\\\)阳光电影.*.ygdy8.com[. ]?(?=[^\\\\]+$)",
			"(?<=\\\\)\\[心中的阳光原创\\](?=[^\\\\]+$)",

			"(?<=\\\\)3D电影\\]\\[k3d.cn\\]\\[完美中字\\](?=[^\\\\]+$)",



			"(?<=\\\\)bbs2048.org出品@(?=[^\\\\]+$)",
			"(?<=\\\\)guochan2048.com -(?=[^\\\\]+$)",
			"(?<=\\\\)\\[电影狗www.dydog.org\\](?=[^\\\\]+$)",
			"(?<=\\\\)\\[99杏\\]\\[香港三级\\](?=[^\\\\]+$)",
			"(?<=\\\\)@\\[香港\\]\\[三级\\](?=[^\\\\]+$)",
			"(?<=\\\\)\\d+\\.\\[香港_?三级\\](?=[^\\\\]+$)",
			"(?<=\\\\)【品色堂p4av.com】\\[?(?=[^\\\\]+$)",
			"(?<=\\\\)香?港(经典)?(三级|影片)[-~]?(?=[^\\\\]+$)",

			"(?<=\\\\)石狮影视论坛www.mndvd.cn@(?=[^\\\\]+$)",
			"(?<=\\\\)石狮影视论坛www.mndvd.cn】(?=[^\\\\]+$)",


			"(?<=\\\\)\\d+@www\\.[a-z0-9.]+@(?=[^\\\\]+$)",
			"(?<=\\\\)第一會所新片@SIS001@(?=[^\\\\]+$)",
			"(?<=\\\\)icao.me@(?=[^\\\\]+$)",
			"(?<=\\\\)2048社区 - \\w{1,3}2048.com@(84)(?=[^\\\\]+$)",
			"(?<=\\\\)JAVVR-Miyu Saito-84(?=[^\\\\]+$)",
			"(?<=\\\\)JAVVR-Nana Fukada-(?=[^\\\\]+$)",

			"(?<=\\\\)mhd1080.com@(?=[^\\\\]+$)",
//			"(?<=\\\\)[a-zA-Z0-9]+\\.com[- ._@](?=[^\\\\]+$)",
//			"(?<=\\\\)[a-zA-Z0-9]+\\.com[^a-zA-Z0-9](?=[^\\\\]+$)",


			"(?<=\\\\)84(?=KMVR-[^\\\\]+$)",
			"(?<=\\\\)84(?=kmvr-[^\\\\]+$)",

			"(?<=\\\\)SLR_(?=[^\\\\]+$)",

			// "(?<=\\\\[^\\\\]{1,99})_1\\[0x1e0\\]_closedCaption_condensed_translaste(?=[^\\\\]+$)",

			"(?<=\\\\)zma-zenra-stark_(?=[^\\\\]+$)",
			"(?<=\\\\)AV文檔(?=[^\\\\]+$)",

			"(?<=\\\\)\\w+.com@(?=[^\\\\]+$)",
			"(?<=\\\\)\\w+\\d{0,4}.org@(?=[^\\\\]+$)",

			"(?<=\\\\)\\w{3,4}.me\\](?=[^\\\\]+$)",
			"(?<=\\\\)\\w{3}.la\\](?=[^\\\\]+$)",

			"(?<=\\\\)\\[xSinsVR.com;(?=[^\\\\]+$)",

			"(?<=\\\\)【南方电影网www.77woo.com】(?=[^\\\\]+$)",
			"(?<=\\\\)1024核工厂_(?=[^\\\\]+$)",


//			"(?<=\\\\)\\(IDEAPOCKET\\)(?=[^\\\\]+$)",
			"(?<=\\\\)\\([A-Z-]+\\)(?=[^\\\\]+$)",




			"(?<=\\\\)影视帝国\\(bbs.cnxp.com\\)\\.(?=[^\\\\]+$)",
			"(?<=\\\\)FLY999原創@單掛D.C.資訊交流網(?=[^\\\\]+$)",
			"(?<=\\\\)FLY999原創@(?=[^\\\\]+$)",
			"(?<=\\\\)第一流氓@18P2P {0,}(?=[^\\\\]+$)",
			"(?<=\\\\)社区 - (?=[^\\\\]+$)",
			"(?<=\\\\)成人劇情片~(?=[^\\\\]+$)",
			"(?<=\\\\)限制級劇情片?~ ?(?=[^\\\\]+$)",
			"(?<=\\\\)韓國限制級~ ?(?=[^\\\\]+$)",
			"(?<=\\\\)劇情四級片~ ?(?=[^\\\\]+$)",
			"(?<=\\\\)劇情片~ ?(?=[^\\\\]+$)",







//			"(?<=\\\\.{1,90})@\\d{3}[a-z]{3}\\.com(?=\\.[^.\\\\]+$)",


//			"(?<=\\\\)\\d{1,3}(?=[^.\\\\\\d]{1,}[^\\\\]+$)",
			"(?<=\\\\)h_\\d{4}(?=[^\\\\]+$)",




			"(?<=\\\\)\\[Arbt.us\\]?@(?=[^\\\\]+$)",
			"(?<=\\\\)\\[?Arbt.xyz\\]?@(?=[^\\\\]+$)",

			"(?<=\\\\)\\[?FHD\\](?=[^\\\\]+$)",

			"(?<=\\\\)\\[fbfb.me\\](?=[^\\\\]+$)",
			"(?<=\\\\)fbfb.me@(?=[^\\\\]+$)",
			"(?<=\\\\)bhd1080.com@(?=[^\\\\]+$)",
			"(?<=\\\\)\\(kawaii\\)(?=[^\\\\]+$)",
			"(?<=\\\\SLR_)SLR (?=[^\\\\]+$)",

			"(?<=\\\\)1030xx.com-(?=[^\\\\]+$)",
			"(?<=\\\\)duopan.LA\\]-(?=[^\\\\]+$)",


			"(?<=\\\\)\\(Madonna\\)(?=[^\\\\]+$)",
			"(?<=\\\\)\\(CASANOVA\\)(?=[^\\\\]+$)",
			"(?<=\\\\)\\(S1\\)(?=[^\\\\]+$)",
			"(?<=\\\\)\\(KMP\\)(?=[^\\\\]+$)",
			"(?<=\\\\)\\(MOODYZ\\)(?=[^\\\\]+$)",
			"(?<=\\\\)\\(PRESTIGE\\)(?=[^\\\\]+$)",
			"(?<=\\\\)\\(V＆R PRODUCE\\)(?=[^\\\\]+$)",
			"(?<=\\\\)\\(KMP(VR)?\\)(?=[^\\\\]+$)",

			"(?<=\\\\.{1,30})-VR(?=\\.[^.\\\\]+$)",






			"(?<=\\\\)SLR_(?=SLR_[^\\\\]+$)",
			"(?<=\\\\SLR_)SLR (?=[^\\\\]+$)",

			"(?<=\\\\.{1,150})\\[fuckbe\\.com\\](?=[^\\\\]+$)",
			"(?<=\\\\.{1,150})[-_ .]?fuckbe[-_ ]?(?=[^\\\\]+$)",

			"(?<=\\\\.{1,150})【HQ超高画質！】(?=[^\\\\]+$)",
			"(?<=\\\\.{1,150})【革新的タイムリープVR】(?=[^\\\\]+$)",
			"(?<=\\\\.{1,150})【VR】(?=[^\\\\]+$)",
			"(?<=\\\\.{1,150})\\(VR\\)(?=[^\\\\]+$)",
			"(?<=\\\\.{1,150})【4K匠】(?=[^\\\\]+$)",
			"(?<=\\\\.{1,150})【4K】(?=[^\\\\]+$)",
			"(?<=\\\\.{1,150})\\[fuckbe\\](?=[^\\\\]+$)",

			"(?<=\\\\.{1,150})\\s*~jujuXOXO(?=[^\\\\]+$)",

			"(?<=\\\\.{1,99})@18p2p(?=\\.[^\\\\]+$)",

			"(?<=\\\\.{1,99})-4k60fps(?=\\.[^\\\\]+$)",
			"(?<=\\\\.{1,99})-4k60fps$",  // for folder

			"(?<=\\\\.{1,99}).4K(?=\\.[^\\\\]+$)",
			"(?<=\\\\.{1,99})_4K(?=\\.[^\\\\]+$)",
			"(?<=\\\\.{1,99})_4K$", // for folder

			"(?<=\\\\.{1,99})\\.XXX(?=\\.[^\\\\]+$)",



			"(?<=\\\\.{1,90})--更多视频访问\\[[^]]+\\](?=\\.[^.\\\\]+$)",
			"(?<=\\\\.{1,90})-2x-RIFE(?=\\.[^.\\\\]+$)",
			"(?<=\\\\.{1,90})-fuckbe.com(?=\\.[^.\\\\]+$)",
			"(?<=\\\\.{1,90})\\.VR(?=\\.[^.\\\\]+$)",
			"(?<=\\\\.{1,90})-RIFE(?=\\.[^.\\\\]+$)",
			"(?<=\\\\.{1,90})\\s+\\([a-zA-Z]+\\.com\\)(?=\\.[^.\\\\]+$)",

			"(?<=\\\\.{1,150})\\.MP4-[A-Z]+\\[rarbg\\](?=\\.[^.\\\\]+$)",

			"(?<=\\\\.{1,90})[~ -_.@]?www\\.[^. \\\\]+\\.com(?=\\.[^.\\\\]+$)",

			"(?<=\\\\.{1,90})[-.\\~]?[a-z0-9]+\\.(com|net)(?=\\.[^.\\\\]+$)",



			"(?<=\\\\)www\\.xBay\\.me\\s+-\\s+(?=[^\\\\]+$)",
			"(?<=\\\\)www\\.IPTV\\.memorial\\s+-\\s+(?=[^\\\\]+$)",

			"(?<=\\\\)\\d+_3xplanet_(?=[^\\\\]+$)",
			"(?<=\\\\)Tokyo\\.Hot\\.(?=[^\\\\]+$)",




			"(?<=\\\\)\\[LS\\](?=[^\\\\]+$)",
			"(?<=\\\\)\\.(?=[^\\\\]+$)",
			"(?<=\\\\)\\,(?=[^\\\\]+$)",
			"(?<=\\\\)\\&(?=[^\\\\]+$)",


			"(?<=\\\\)\\w+@18p2p(@022)? {0,}(?=[^\\\\]+$)",
			"(?<=\\\\)d4b4\\.com(?=[^\\\\]+$)",
			"(?<=\\\\)t3u3\\.com(?=[^\\\\]+$)",

			"(?<=\\\\)[a-z]\\d+\\.4KMV-(?=[^\\\\]+$)",
//			"(?<=\\\\)[a-z]\\d+\\.(?=[^\\\\]+\\.[^.\\\\]+$)",
			"(?<=\\\\)60FPS - (?=[^\\\\]+$)",



			"(?<=\\\\)Marica.Hase\\s+-\\s+(?=[^\\\\]+$)",
			"(?<=\\\\)Marica.Hase\\s?(?=[^\\\\]+$)",
			"(?<=\\\\)h.d\\d00.com@(?=[^\\\\]+$)",
			"(?<=\\\\)\\[ThZu\\.Cc\\](?=[^\\\\]+$)",
//			"(?<=\\\\)\\[[^\\]]+.com\\][. ]?(?=[^\\\\]+$)",
			"(?<=\\\\)w2yuqing@(?=[^\\\\]+$)",
			"(?<=\\\\)FISH321@18P2P {0,}(?=[^\\\\]+$)",
			"(?<=\\\\)【每日更新[^】]*】(?=[^\\\\]+$)",
			"(?<=\\\\)【Weagogo】(?=[^\\\\]+$)",
			"(?<=\\\\)\\(中文字幕\\)(?=[^\\\\]+$)",
			"(?<=\\\\)\\(映画\\)(?=[^\\\\]+$)",



			"(?<=\\\\)香港：(?=[^\\\\]+$)",
			"(?<=\\\\)\\[更多电影尽在outdy.com\\] ?\\[?(?=[^\\\\]+$)",
			"(?<=\\\\)wws1983@18P2P(?=[^\\\\]+$)",
			"(?<=\\\\)www\\.[a-z]+[0-9]+\\.xyz(?=[^\\\\]+$)",
			"(?<=\\\\)\\[BT-btt.com\\](?=[^\\\\]+$)",
			"(?<=\\\\)mfgc2\\.com (?=[^\\\\]+)",
			"(?<=\\\\)mfgc7\\.com (?=[^\\\\]+)",
			"(?<=\\\\)QueenSnake - (?=[^\\\\]+)",
			"(?<=\\\\)Queensnake_-_(?=[^\\\\]+)",
			"(?<=\\\\)QueenSnake_(?=[^\\\\]+)",




			// TODO: comment out below one line
//			"(?<=\\\\)\\[[^]]+\\] {0,}(?=[^\\\\]+$)",
			"(?<=\\\\)\\[a-zA-Z0-9.]+\\] {0,}(?=[^\\\\]+$)",
			"(?<=\\\\)【[^】]+】(?=[^\\\\]+$)",
//			"(?<=\\\\)[【\\[《]+(?=[^\\\\]+$)",
//			"(?<=\\\\)\\((?=[^\\\\]+$)",


//			"(?<=\\\\)\\d{3,}(?=[^\\\\]+$)",
			"(?<=\\\\)\\d{1}\\.(?=[^\\\\]+\\.[^.\\\\]$)",

			"(?<=\\\\.{1,150}\\d{3})pl(?=\\.[^\\\\]+)"
	};


	private static String[] idPrefixRegs = new String [] {
			"^[a-z0-9]{1,7}-\\d{3,4}(?=[-._ ][^\\\\]+$)",
			"^[a-z0-9]{1,7}-\\d{3,4}(?=$)",
//			"^VRBangers(?=[-._ ][^\\\\]+$)",
			"^swallowbay(?=[-._ ][^\\\\]+$)",
//			"^czechvrfetish(?=[-._ ][^\\\\]+$)",


			"^fc2[ _-]*ppv(?=[-._ ][^\\\\]+$)"


	};

	private static String[][] adReplaceRegs = new String [][] {
//		{"(?<=\\\\\\w{1,9})0+(?=[1-9]\\d{0,9}[^\\\\]+$)", "-"},
		{"(?<=\\\\\\w{1,9}[a-z])0{2}(?=\\d{3}[^\\d][^\\\\]+$)", "-"},      // abc00123.mp4 => abc-123.mp4
		{"(?<=\\\\\\w{1,9}[a-z])0(?=\\d{2}[^\\d][^\\\\]+$)", "-0"},      // abc023.mp4 => abc-023.mp4

		{"(?<=\\\\\\w{1,9}[a-z])([1-9]\\d{3})(?=[a-z]\\.[^\\\\]+$)", "-$1-"},      // dsvr1234a.mp4 => dsvr-1234-A.mp4

		// the_suckubus_ORIGINAL_ENCODEDp_vrconk__180_lr.mp4
		{"(?<=\\\\)([^\\\\]*[^_]{1,99})(_{1,9}vrconk_{1,9})(?=[^\\\\]+$)", "VRConk_$1_"},


		{"(?<=\\\\)(\\[VRBangers.com\\])[ _-]{0,9}(?=[^\\\\]+$)", "VRBangers_"},
		{"(?<=\\\\VirtualRealPorn)\\.com[ _-]{0,9}(?=[^\\\\]+$)", "_"},

		{"(?<=\\\\)(\\[VRCosplayX.com\\])[ _-]{0,9}(?=[^\\\\]+$)", "VRCosplayX_"},


		{"(?<=\\\\)(\\[POVR.com;POVROriginals(.com)?\\])[ _-]{0,9}(?=[^\\\\]+$)", "POVROriginals_"},

		{"(?<=\\\\.{1,150})\\.part(\\d+)\\s+(?=[^\\\\]+$)", "-$1"}, // HUNVR-107.part1 - 7 Babes Fucked In A Share House.mp4 => HUNVR-107-1- 7 Babes Fucked In A Share House.mp4

		{"(?<=\\\\[a-z0-9]{1,7}-\\d{3,4})[-._ ]p(\\d+)\\s+(?=[^\\\\]+$)", "-$1-"}, // MDVR-092 P2 HITOMI H265_2048p_180_LR_3dh.mp4 => MDVR-092-2-HITOMI H265_2048p_180_LR_3dh.mp4

		{"(?<=\\\\.{1,99})_4K-(?=[^\\\\]+$)", "-"},
		{"(?<=\\\\.{1,99}) \\[HQ-VR\\] (?=[^\\\\]+$)", "-"},
		{"(?<=\\\\.{1,99}\\d{3,5})hhb(?=\\d{1,}[^\\\\]+$)", "-"},
		{"(?<=\\\\.{1,99}-)000(?=\\d{1,}[^\\\\]+$)", "0"},
		{"(?<=\\\\.{1,150})(\\.|-)(part|pt|R|CD)(\\d{1,2})(?=\\.[^\\\\]+$)", "-$3"},
		{"(?<=\\\\.{1,150})\\s{1,99}part\\s{1,99}(\\d{1,2})(?=\\.[^\\\\]+$)", "-$1"},


		{"(?<=\\\\.{1,99})-R(\\d{1,2})(?=\\.[^\\\\]+$)", "-$1"}, // abc-R1.mp4 => abc-1.mp4
		{"(?<=\\\\.{1,99})-R(\\d{1,2})(?=_[^\\\\]+$)", "-$1"}, // abc-R1_TB.mp4 => abc-1_TB.mp4


		{"(?<=\\\\)\\[([a-zA-Z0-9-]+)\\][ _-]{0,9}(?=[^\\\\]+$)", "$1-"},
		{"(?<=\\\\)\\(([a-zA-Z0-9-]+)\\)[ _-]{0,9}(?=[^\\\\]+$)", "$1-"},

		{"(?<=\\\\)\\[([^]]+)\\](?=[^\\\\]+$)", "$1"},   // [abc]def.mp4  => abcdef.mp4
		{"(?<=\\\\)《([^]]+)》(?=[^\\\\]+$)", "$1"},   // 《abc》def.mp4  => abcdef.mp4


//		{"(?<=\\\\)(\\d{1})\\.([\\.\\][^\\\\]+)(?=\\.[^.\\\\]+$)", "$2-$1"},


		{"(?<=\\\\.{1,150})(-|_)A(?=\\.[^\\\\]+$)", "-1"},
		{"(?<=\\\\.{1,150})(-|_)B(?=\\.[^\\\\]+$)", "-2"},
		{"(?<=\\\\.{1,150})(-|_)C(?=\\.[^\\\\]+$)", "-3"},
		{"(?<=\\\\.{1,150})(-|_)D(?=\\.[^\\\\]+$)", "-4"},
		{"(?<=\\\\.{1,150})(-|_)E(?=\\.[^\\\\]+$)", "-5"},
		{"(?<=\\\\.{1,150})(-|_)F(?=\\.[^\\\\]+$)", "-6"},
		{"(?<=\\\\.{1,150})(-|_)G(?=\\.[^\\\\]+$)", "-7"},
		{"(?<=\\\\.{1,150})(-|_)H(?=\\.[^\\\\]+$)", "-8"},
		{"(?<=\\\\.{1,150})(-|_)I(?=\\.[^\\\\]+$)", "-9"},
		{"(?<=\\\\.{1,150})(-|_)J(?=\\.[^\\\\]+$)", "-10"},
		{"(?<=\\\\.{1,150})(-|_)K(?=\\.[^\\\\]+$)", "-11"},
		{"(?<=\\\\.{1,150})(-|_)L(?=\\.[^\\\\]+$)", "-12"},
		{"(?<=\\\\.{1,150})(-|_)M(?=\\.[^\\\\]+$)", "-13"},
		{"(?<=\\\\.{1,150})(-|_)N(?=\\.[^\\\\]+$)", "-14"},
//
//		{"(?<=\\\\.{1,150})_A(?=\\.[^\\\\]+$)", "-1"},
//		{"(?<=\\\\.{1,150})_B(?=\\.[^\\\\]+$)", "-2"},
//		{"(?<=\\\\.{1,150})_C(?=\\.[^\\\\]+$)", "-3"},
//		{"(?<=\\\\.{1,150})_D(?=\\.[^\\\\]+$)", "-4"},
//		{"(?<=\\\\.{1,150})_E(?=\\.[^\\\\]+$)", "-5"},
//		{"(?<=\\\\.{1,150})_F(?=\\.[^\\\\]+$)", "-6"},
//		{"(?<=\\\\.{1,150})_G(?=\\.[^\\\\]+$)", "-7"},
//		{"(?<=\\\\.{1,150})_H(?=\\.[^\\\\]+$)", "-8"},
//		{"(?<=\\\\.{1,150})_I(?=\\.[^\\\\]+$)", "-9"},
//		{"(?<=\\\\.{1,150})_J(?=\\.[^\\\\]+$)", "-10"},
//		{"(?<=\\\\.{1,150})_K(?=\\.[^\\\\]+$)", "-11"},
//		{"(?<=\\\\.{1,150})_L(?=\\.[^\\\\]+$)", "-12"},
//		{"(?<=\\\\.{1,150})_M(?=\\.[^\\\\]+$)", "-13"},
//		{"(?<=\\\\.{1,150})_N(?=\\.[^\\\\]+$)", "-14"},

		{"(?<=\\\\[a-zA-Z0-9]{2,7}-\\d{3}) (?=\\d{1,2}\\.[^\\\\]+$)", "-"},  //  abc-098 1.mp4  => abc-1.mp4
		{"(?<=\\\\[a-zA-Z0-9]{2,7}-\\d{3})([a-z])(?=\\.[^\\\\]+$)", "-$1"},  //  abc-098a.mp4  => abc098-a.mp4


		{"(?<=\\\\)(\\[日本剧情\\])(.*)(?=\\.[^.\\\\]+$)", "$2$1"},
		{"(?<=\\\\)(\\[日本\\])(.*)(?=\\.[^.\\\\]+$)", "$2$1"},
		{"(?<=\\\\)(\\[韩国三级\\])(.*)(?=\\.[^.\\\\]+$)", "$2$1"},

		{"(?<=\\\\)(DSVR-)(.*)(?=\\.[^.\\\\]+$)", "3$1$2"},   // DSVR-001-1 -> 3DSVR-001-1

		{"(?<=\\\\)1(3dsvr)(?=.*\\.[^.\\\\]+$)", "3DSVR"},   // DSVR-001-1 -> 3DSVR-001-1

		{"(?<=\\\\)3dsvr-(\\d{3})-(?=[^\\\\]+\\.[^.\\\\]+$)", "3DSVR-0$1-"},    // 3dsvr-123-1.mp4   => 3dsvr-0123-1.mp4
		{"(?<=\\\\)3dsvr0(\\d{4})-(?=[^\\\\]+\\.[^.\\\\]+$)", "3DSVR-$1-"},     // 3dsvr01234-1.mp4 => 3dsvr-1234-1.mp4
		{"(?<=\\\\\\w{1,9}[a-z])0(?=\\d{4}-[^\\\\]+$)", "-"},      // abc01234-1.mp4 => abc-1234-1.mp4

//		13dsvr01138

		{"(?<=\\\\.{1,150}vr)(\\d{3})([a-zA-Z])(?=\\.[^\\\\]+$)", "-$1-$2"},   // bibivr048A.mp4 -> bibivr-048-A.mp4

		{"(?<=\\\\)(3D) (.*)(?=\\.[^.\\\\]+$)", "$2_$1"},
		{"(?<=\\\\)(无码流出)[ _-]*(.*)(?=\\.[^.\\\\]+$)", "$2_$1"},

		{"(?<=\\\\)(CD\\d)[ .](.*)(?=\\.[^.\\\\]+$)", "$2_$1"},

		{"(?<=\\\\)(FC2)[ _-]*(PPV)[ _-](.*)(?=\\.[^.\\\\]+$)", "$1-$2-$3"},

		{"(?<=\\\\)(FC2)[ _-]*(\\d{6,7})[ _-](.+)(?=\\.[^.\\\\]+$)", "FC2-PPV-$2-$3"},
		{"(?<=\\\\)(fc2)[ _-]*(\\d{6,7})[ _-](.+)(?=\\.[^.\\\\]+$)", "FC2-PPV-$2-$3"},
		{"(?<=\\\\)(FC2)[ _-]*(\\d{6,7})(?=\\.[^.\\\\]+$)", "FC2-PPV-$2"},
		{"(?<=\\\\)(fc2)[ _-]*(\\d{6,7})(?=\\.[^.\\\\]+$)", "FC2-PPV-$2"},



		{"(?<=\\\\)povr[-\\.]originals(?=[-_ \\.][^\\\\]+\\.[^.\\\\]+$)", "POVROriginals"},


		// handle vac
		{"(?<=\\\\)(vac-vrb)(?=\\d{6}[^\\\\]+\\.[^.\\\\]+$)", "VRBangers-$1"},
		{"(?<=\\\\)(vac-vrh)(?=\\d{6}[^\\\\]+\\.[^.\\\\]+$)", "VRHush-$1"},
		{"(?<=\\\\)(vac-vrp)(?=\\d{6}[^\\\\]+\\.[^.\\\\]+$)", "VirtualRealPorn-$1"},
		{"(?<=\\\\)(vac-vt)(?=\\d{6}[^\\\\]+\\.[^.\\\\]+$)", "VirtualTaboo-$1"},
		{"(?<=\\\\)(vac-slr)(?=\\d{6}[^\\\\]+\\.[^.\\\\]+$)", "SLR-$1"},
		{"(?<=\\\\)(vac-pg)(?=\\d{6}[^\\\\]+\\.[^.\\\\]+$)", "NaughtyAmerica-PartyGirls-$1"},
		{"(?<=\\\\)(vac-rpvr)(?=\\d{6}[^\\\\]+\\.[^.\\\\]+$)", "NaughtyAmerica-RealPornstars-$1"},


		{"(?<=\\\\)(Classroom\\.)(?=[^\\\\]+$)", "NaughtyAmerica-$1"},
		{"(?<=\\\\)(DressingRoom\\.)(?=[^\\\\]+$)", "NaughtyAmerica-$1"},
		{"(?<=\\\\)(FANS\\.)(?=[^\\\\]+$)", "NaughtyAmerica-$1"},
		{"(?<=\\\\)(Gym\\.)(?=[^\\\\]+$)", "NaughtyAmerica-$1"},
		{"(?<=\\\\)(DressingRoom\\.)(?=[^\\\\]+$)", "NaughtyAmerica-$1"},
		{"(?<=\\\\)(DormRoom\\.)(?=[^\\\\]+$)", "NaughtyAmerica-$1"},
		{"(?<=\\\\)(DressingRoom\\.)(?=[^\\\\]+$)", "NaughtyAmerica-$1"},
		{"(?<=\\\\)(MyFriendsHotMom\\.)(?=[^\\\\]+$)", "NaughtyAmerica-$1"},
		{"(?<=\\\\)(MyWifesHotFriend\\.)(?=[^\\\\]+$)", "NaughtyAmerica-$1"},
		{"(?<=\\\\)(Classroom\\.)(?=[^\\\\]+$)", "NaughtyAmerica-$1"},
		{"(?<=\\\\)(Office\\.)(?=[^\\\\]+$)", "NaughtyAmerica-$1"},
		{"(?<=\\\\)(PartyGirls\\.)(?=[^\\\\]+$)", "NaughtyAmerica-$1"},
		{"(?<=\\\\)(PSEPornStarExperience\\.)(?=[^\\\\]+$)", "NaughtyAmerica-$1"},
		{"(?<=\\\\)(SummerVacation\\.)(?=[^\\\\]+$)", "NaughtyAmerica-$1"},
		{"(?<=\\\\)(Spa\\.)(?=[^\\\\]+$)", "NaughtyAmerica-$1"},
		{"(?<=\\\\)(TandA\\.)(?=[^\\\\]+$)", "NaughtyAmerica-$1"},
		{"(?<=\\\\)(SuperSluts\\.)(?=[^\\\\]+$)", "NaughtyAmerica-$1"},
		{"(?<=\\\\)(DirtyWivesClub\\.)(?=[^\\\\]+$)", "NaughtyAmerica-$1"},
		{"(?<=\\\\)(AmericanDaydreams\\.)(?=[^\\\\]+$)", "NaughtyAmerica-$1"},
		{"(?<=\\\\)(DirtyWivesClub\\.)(?=[^\\\\]+$)", "NaughtyAmerica-$1"},
		{"(?<=\\\\)(MyFirstSexTeacher\\.)(?=[^\\\\]+$)", "NaughtyAmerica-$1"},
		{"(?<=\\\\)(AfterSchool\\.)(?=[^\\\\]+$)", "NaughtyAmerica-$1"},
		{"(?<=\\\\)(MyFirstSexTeacher\\.)(?=[^\\\\]+$)", "NaughtyAmerica-$1"},
		{"(?<=\\\\)(AfterSchool\\.)(?=[^\\\\]+$)", "NaughtyAmerica-$1"},





		//TODO: comment out below line
//		{"(?<=\\\\.{1,99})【[^】]+】(?=[^\\\\]+$)", " "},
//		{"(?<=\\\\)\\[([^\\]]+)\\][. -]?(?=[^\\\\]{0,}\\.[^\\\\]+$)", "$1_"},      // [abc]de.txt => abd_de.txt
//		{"(?<=\\\\)\\(([^\\)\\\\]+)\\)[. -]?(?=[^\\\\]{0,}\\.[^\\\\]+$)", "$1_"},  // (abc)de.txt => abd_de.txt

//		{"(?<=\\\\.{1,99})(-\\d+)([a-z])(?=[^\\\\]+$)", "$1-$2"},

	};


	public static File removeAdsPrefix(File file) {
		String oriAbsPath = file.getPath();
		String newAbsPath = oriAbsPath;

//		ComLogUtil.info("oriAbsPath: " + oriAbsPath);
//		ComLogUtil.info("oriAbsPath over");

		for(int i = 0; i < adPrefixRegs.length; i++) {
			String currentReg = adPrefixRegs[i];

//			if(currentReg.indexOf("rarbg") > -1) {
//				ComLogUtil.info("oriAbsPath: " + oriAbsPath + ", currentReg:" + currentReg);
//			}

//			ComLogUtil.info("oriAbsPath: " + oriAbsPath + ", currentReg:" + currentReg);
			try {
				newAbsPath = ComRegexUtil.replaceByRegexI(oriAbsPath, currentReg, "");
			} catch(Exception e) {
				ComLogUtil.error("regex error on:" + currentReg);
				throw e;
			}



			if(!oriAbsPath.equals(newAbsPath)) { // only do rename when needed
				File newFile = ComRenameUtil.findAndAddNumberSuffix(new File(newAbsPath));
				boolean renameRet = (!isPrintOnly ? file.renameTo(newFile) : false);
				String logStr = "Need to rename from/to" + " ret:" + renameRet + " by reg: " + currentReg + "\n" + oriAbsPath + "\n" + newFile;
				if(renameRet) {
					ComLogUtil.error(logStr);
					return newFile;
				} else {
					ComLogUtil.error(logStr);
					return file;
				}
			}
		}
		return file;
	}

	public static File removePreSuffSpace(File file) {
		String oriAbsPath = file.getPath();

		// remove prefix space
//		String newAbsPath = ComRegexUtil.replaceByRegexI(oriAbsPath, "(?<=\\\\)\\s+[^ ](?=[^\\\\]+)", "");
		String newAbsPath = ComRegexUtil.replaceByRegexI(oriAbsPath, "(?<=\\\\)\\s+(?=[^\\\\]+$)", "");

		// remove trailing space
		newAbsPath = ComRegexUtil.replaceByRegex(newAbsPath, "(?<=\\\\[^ \\\\]{0,99})\\s+(?=\\.[^ \\.\\\\]+)", "");

		if(!oriAbsPath.equals(newAbsPath)) { // only do rename when needed
			File newFile = ComRenameUtil.findAndAddNumberSuffix(new File(newAbsPath));
			boolean renameRet = (!isPrintOnly ? file.renameTo(newFile) : false);
			String logStr = "rename\n" + oriAbsPath + "\nto\n" + newFile + ":" + renameRet;
			if(renameRet) {
				ComLogUtil.error(logStr);
				return newFile;
			} else {
				ComLogUtil.info(logStr);
				return file;
			}
		}
		return file;
	}

	public static void video2Mp4(File file) {
		String oriAbsPath = file.getPath();
		String fileExtension = ComFileUtil.getFileExtension(file, false);

		if("webm".equalsIgnoreCase(fileExtension)) { // only do rename when needed
			ComLogUtil.info("will rename oriAbsPath to mp4");
			if(!isPrintOnly) {
				File newFile = ComRenameUtil.replaceFileExtention(file, ".mp4");
			}

		}
	}

	public static File replaceAds(File file) {
		String oriAbsPath = file.getPath();
		String newAbsPath = oriAbsPath;

//		ComLogUtil.info("oriAbsPath: " + oriAbsPath);
//		ComLogUtil.info("oriAbsPath over");

		for(int i = 0; i < adReplaceRegs.length; i++) {
			String[] currentRules = adReplaceRegs[i];
			String currentReg = currentRules[0];
			String currentReplacement = currentRules[1];
//			ComLogUtil.info("oriAbsPath: " + oriAbsPath + ", currentReg:" + currentReg);
			newAbsPath = ComRegexUtil.replaceByRegexIGroup(oriAbsPath, currentReg, currentReplacement);

			if(!oriAbsPath.equals(newAbsPath)) { // only do rename when needed
				File newFile = ComRenameUtil.findAndAddNumberSuffix(new File(newAbsPath));
				boolean renameRet = (!isPrintOnly ? file.renameTo(newFile) : false);
				String logStr = "Need to rename from/to" + ":" + renameRet + " by reg: " + currentReg + ", replacement: " + currentReplacement + "\n" + oriAbsPath + "\n" + newFile;
				if(renameRet) {
					ComLogUtil.error(logStr);
					return newFile;
				} else {
					ComLogUtil.error(logStr);
					return file;
				}
			}
		}
		return file;
	}

	private static void doOneLevel(File dir) throws Exception {
		File[] files = dir.listFiles();
        if(files == null) {
        	ComLogUtil.error("listed files is null, maybe the explorer.exe is hold the handler of this empty dir. dir:" + dir);
        	return;
        }
        if(files.length == 0) {
        	ComLogUtil.error("will remove this empty dir:" + dir);
        	if(!isPrintOnly) ComFileUtil.delFileAndFolder(dir);
        	return;
        } else {
//        	ComLogUtil.info("won't remove this none-empty dir:" + dir);
        }
		int length = files.length;
		List<File> folders = new ArrayList<File>();
		for(int i = 0; i < length; i++) {
			File file = files[i];
			File originFile = file;
			String getAbsolutePath = file.getPath();
			String nameOnly = file.getName();
//			ComLogUtil.info("file1:" + file.getAbsolutePath());
//			ComLogUtil.info("file2:" + file.getName());
//			ComLogUtil.info("file3:" + file.getPath());

			if(file.isDirectory()) {
				folders.add(file);
			} else {
				if(originFile == file) file = removeAdsPrefix(file);
				if(originFile == file) file = removePreSuffSpace(file);
				if(originFile == file) file = replaceAds(file);
//				if(originFile == file) removeAdsPrefix(file);
//				if(originFile == file) removePreSuffSpace(file);
				if(originFile == file) file = UppercaseVideoID(file);
				if(originFile == file) file = AppendVideoResolution(file);
//				if(originFile == file) file = AppendVideoDuration(file);

			}
		}


		int size = folders.size();
		for(int i = 0; i < size; i++) {
			File nextFolder = folders.get(i);

			String absolutePath = nextFolder.getPath();
			String nextFolderName = nextFolder.getName();
			if(ComRegexUtil.test(nextFolderName, "_SKIP(_KEEP)?$")) {
				// do nothing for disk folder
				ComLogUtil.info("skip for skip folder: " + nextFolderName);
			} else {
				doOneLevel(nextFolder);
			}
		}
	}

	private static File AppendVideoResolution(File file) throws Exception {
		if(!ComMediaUtil.isVideo(file)) {
			// video resolution is only available for video files.
			return file;
		}
		if(!isAppendResolution) {
			// this feature is not enabled
			return file;
		}

		if(!file.exists()) {
			return file;
		}

		FileName fileName = new FileName(file);
		String fileNameOnly = fileName.getFileNameOnly();
		Boolean isVideoResolutionAlreadyAdded =
//				fileNameOnly, "[_ -.](?!180x180)\\d{3,4}x\\d{3,4}[. _]") // PVRStudio_5760x2880_.mp4
//				|| ComRegexUtil.test(fileNameOnly, "[_ -](?!180x180)\\d{3,4}x\\d{3,4}$")
				ComRegexUtil.test(fileNameOnly, "[_ -](?!180x180)\\d{3,4}x\\d{3,4}([. _]|$)") // PVRStudio_5760x2880_.mp4 PVRStudio_5760x2880.mp4
//				|| ComRegexUtil.testIg(fileNameOnly, "[- _.][4-8]k[- _.]")
//				|| ComRegexUtil.testIg(fileNameOnly, "[- _.][4-8]k$")
				|| ComRegexUtil.testIg(fileNameOnly, "[- _.][4-8]k([- _.]|$)")  // PVRStudio_8k_.mp4 PVRStudio_8k.mp4
//				|| ComRegexUtil.testIg(fileNameOnly, "[- _.][1-9]\\d{3}p[- _.]")
//				|| ComRegexUtil.testIg(fileNameOnly, "[- _.][1-9]\\d{3}p$")
				|| ComRegexUtil.testIg(fileNameOnly, "[- _.][1-9]\\d{3}p([- _.]|$)")  // PVRStudio_4096p_.mp4 PVRStudio_4096p.mp4
				;
		File ret = file;

		if(!isVideoResolutionAlreadyAdded) {
			VideoResolution videoResolution = ComMediaUtil.getVideoResolution(file);
			fileName.append("_" + videoResolution);
			ret = fileName.toFile();
			ComFileUtil.doRename(!isPrintOnly, file, ret, "by videoSolution");
		}

		return ret;
	}

	private static File AppendVideoDuration(File file) throws Exception {
		// TODO:
		throw new Exception("not implemented");
	}

	private static File UppercaseVideoID(File file) throws Exception {
		FileName fileName = new FileName(file);
		String fileNameOnly = fileName.getFileNameOnly();


		String videoID = "";
		File ret = file;

		for(int i = 0; i < idPrefixRegs.length; i++) {
			String currentReg = idPrefixRegs[i];

			try {
				videoID = ComRegexUtil.getMatchedString(fileNameOnly, currentReg);
				if(!ComStrUtil.isBlankOrNull(videoID)) {
					String videoIDUpperCased = videoID.toUpperCase();
					String newfileNameOnly = videoIDUpperCased + fileNameOnly.substring(videoID.length());
					fileName.setFileName(newfileNameOnly);
					ret = fileName.toFile();
					ComFileUtil.doRename(!isPrintOnly, file, ret, "by uppercase");
					break;
				}

			} catch(Exception e) {
				ComLogUtil.error("regex error on:" + currentReg);
				throw e;
			}
		}
//
//		String videoID = ComRegexUtil.getMatchedString(fileNameOnly, "^[a-z0-9]{1,7}-\\d{3}(?=[-_ ][^\\\\]+$)");
//		videoID = ComRegexUtil.getMatchedString(fileNameOnly, "^fc2[ _-]*ppv(?=[-_ ][^\\\\]+$)");
//
//		if(!ComStrUtil.isBlankOrNull(videoID)) {
//			String videoIDUpperCased = videoID.toUpperCase();
//			String newfileNameOnly = videoIDUpperCased + fileNameOnly.substring(videoID.length());
//			fileName.setFileName(newfileNameOnly);
//			ret = fileName.toFile();
//			ComFileUtil.doRename(!isPrintOnly, file, ret, "by uppercase");
//		}

		return ret;
	}

	private static File dupSuffixToNumber(File file) {
		String oriAbsPath = file.getPath();
		String newAbsPath = oriAbsPath;

//		ComLogUtil.info("oriAbsPath: " + oriAbsPath);
//		ComLogUtil.info("oriAbsPath over");

		String currentReg = "(?<=\\\\.{1)-VR(?=\\.[^.\\\\]+$)";
		String currentReplacement = "-$1";
//			ComLogUtil.info("oriAbsPath: " + oriAbsPath + ", currentReg:" + currentReg);
		newAbsPath = ComRegexUtil.replaceByRegexIGroup(oriAbsPath, currentReg, currentReplacement);

		if(!oriAbsPath.equals(newAbsPath)) { // only do rename when needed
			File newFile = ComRenameUtil.findAndAddNumberSuffix(new File(newAbsPath));
			boolean renameRet = (!isPrintOnly ? file.renameTo(newFile) : false);
			String logStr = "Need to rename from/to" + ":" + renameRet + " by reg: " + currentReg + ", replacement: " + currentReplacement + "\n" + oriAbsPath + "\n" + newFile;
			if(renameRet) {
				ComLogUtil.error(logStr);
				return newFile;
			} else {
				ComLogUtil.error(logStr);
				return file;
			}
		}
		return file;
	}


}

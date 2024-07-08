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
	private static Integer appendResultionIfFirstNumberGreaterThan = 0;

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
		
		appendResultionIfFirstNumberGreaterThan = Integer.parseInt(configManager.getString("appendResultionIfFirstNumberGreaterThan"), 10);

		doOneLevel(AdsVideoRm.convertToArrDir(FolderToHandle));
	}

	private static String[] adPrefixRegs = new String [] {
			
			"(?<=\\\\)\\(SAMURAI PORN\\)[-_ ]*(?=[^\\\\]+$)",
			"(?<=\\\\)mw6.me[-_ @]*(?=[^\\\\]+$)",
			"(?<=\\\\)sheqing[-_ @]*(?=[^\\\\]+$)",
			"(?<=\\\\)店長推薦作品[-_ @]*(?=[^\\\\]+$)",
			"(?<=\\\\)店长推荐作品[-_ @]*(?=[^\\\\]+$)",
			"(?<=\\\\)店长推荐[-_ @]*(?=[^\\\\]+$)",
			"(?<=\\\\)[a-z0-9]+.xyz[-_ @]*(?=[^\\\\]+$)",
			"(?<=\\\\)boy100[-_ @]*(?=[^\\\\]+$)",
			"(?<=\\\\)[^\\\\]+@(第一会所|第一會所|草榴社區)@[-_ @]*(?=[^\\\\]+$)",
			"(?<=\\\\)[a-z0-9]+\\.(com|cc|net)[-_ ]*(?=[^\\\\]+$)",
			
			"(?<=\\\\)●+(?=[^\\\\]+$)",
			"(?<=\\\\)Uncensored[-_ ]*Leaked[-_ ]*(?=[^\\\\]+$)",
			"(?<=\\\\)\\( *Uncensored[-_ ]*Leaked *\\)[-_ ]*(?=[^\\\\]+$)",
			"(?<=\\\\)\\[ *Uncensored[-_ ]*(Leaked|HD) *\\][-_ ]*(?=[^\\\\]+$)",
			"(?<=\\\\)無修正[-_ ]*(漏れ|流出|リーク)?[-_ ]*(?=[^\\\\]+$)",
			"(?<=\\\\)\\(無修正[-_ ]*(漏れ|流出|リーク)?\\)[-_ ]*(?=[^\\\\]+$)",
			"(?<=\\\\)\\[Xav-7\\.Xyz\\][-_ ]*(?=[^\\\\]+$)",
			"(?<=\\\\)\\[MyXav\\.Pw\\][-_ ]*(?=[^\\\\]+$)",
			"(?<=\\\\)\\[www.[a-z][0-9]+\\.(com|cc|net)\\][-_ ]*(?=[^\\\\]+$)",
			"(?<=\\\\)\\[[a-z][0-9]+\\.(com|cc|net)\\][-_ ]*(?=[^\\\\]+$)",
			
			"(?<=\\\\)HTCdesireHD@(?=[^\\\\]+$)",
			
			"(?<=\\\\)SAMURAI PORN[-_ \\(]+(?=[^\\\\]+$)",
			
			"(?<=\\\\)\\[HD JAV Uncensored\\][ _-]*(?=[^\\\\]+$)",
			
			
			
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
			"(?<=\\\\)AVFAP\\.NET[-_ ](?=[^\\\\]+$)",
			"(?<=\\\\)\\d{6}\\.xyz[-_ ](?=[^\\\\]+$)",
			"(?<=\\\\)h_910(?=[^\\\\]+$)",
			


			"(?<=\\\\)202\\d{5}r\\.(?=[^\\\\]+$)",  // 20200329r.(AfestaVR)(NHVR-077-4K.vti9nk3v)【4K】心の声が聞出張先で相部屋になった女上司に試欲求不満な本音が丸聞こえで…。-1.jpg

			"(?<=\\\\)AfestaVR-(?=[^\\\\]+$)",
			"(?<=\\\\)60FPS\\(AIフレーム補間\\) \\[VR\\] (?=[^\\\\]+$)",
			"(?<=\\\\)69av-(?=[^\\\\]+$)",
			"(?<=\\\\)\\(E-BODY VR\\)(?=[^\\\\]+$)",
			"(?<=\\\\)\\(FALENO VR\\)(?=[^\\\\]+$)",
			"(?<=\\\\)\\(FAVE MODE\\)(?=[^\\\\]+$)",
			"(?<=\\\\)\\(MONDELDE VR\\)(?=[^\\\\]+$)",
			"(?<=\\\\)\\(SPICY VR\\)(?=[^\\\\]+$)",
			"(?<=\\\\)\\(WAAP VR\\)(?=[^\\\\]+$)",
			"(?<=\\\\)\\(ブイワンVR\\)(?=[^\\\\]+$)",
			"(?<=\\\\)\\(本中 VR\\)(?=[^\\\\]+$)",
			"(?<=\\\\)\\(Fitch肉感VR\\)(?=[^\\\\]+$)",
			
			"(?<=\\\\)1(?=FSVSS-\\d{3,4}[^\\\\]+$)",
			"(?<=\\\\)\\d{1,3}(?=DTVR-\\d{3,4}[^\\\\]+$)",
			"(?<=\\\\)h_\\d{3}(?=fsvr-\\d{3,4}[^\\\\]+$)",

			"(?<=\\\\)1(?=nhvr[^\\\\]+$)",
			"(?<=\\\\)AF-(?=CRVR[^\\\\]+$)",
			"(?<=\\\\)huigezai *东京(熱|热) *(?=[^\\\\]+$)",


			"(?<=\\\\)Tokyo.{0,1}Hot[-_ ]{1}(?=[^\\\\]+)",
			"(?<=\\\\)\\[thz\\.la\\](?=[^\\\\]+)",
			"(?<=\\\\)\\[c0e0\\.com\\](?=[^\\\\]+)",
			"(?<=\\\\)91制片厂(最新出品)? ?(?=[^\\\\]+$)",
			"(?<=\\\\)(最新)?国产AV佳作 ?(?=[^\\\\]+$)",
			"(?<=\\\\)\\[[^\\]]*(电影|下载|BT)[^\\]]*(www|bbs).[a-zA-Z0-9]+.(com|net|cc|cn|org)\\][. -]?(?=[^\\\\]+$)",
			"(?<=\\\\)\\[[a-zA-Z0-9]+.(com|net|cc|cn|org)[^\\]]*电影[^\\]]*\\][. ]?(?=[^\\\\]+$)",
			"(?<=\\\\)2048论坛@fun2048.com -[-_ ]*(?=[^\\\\]+$)",
			"(?<=\\\\)one2048.com[-_ ]*(?=[^\\\\]+$)",
			"(?<=\\\\)hjd204?8\\.com[-_ ]*(?=[^\\\\]+$)",
			"(?<=\\\\)[a-z0-9]+\\.com[-_ @]*(?=[^\\\\]+$)",
			
			"(?<=\\\\)(www|bbs|hd).[a-zA-Z0-9]+.(com|net|cc|cn|org)[@. -]?(?=[^\\\\]+$)",
			"(?<=\\\\)\\[(www|bbs|hd).[a-zA-Z0-9]+.(com|net|cc|cn|org)\\][@. -]?(?=[^\\\\]+$)",
			"(?<=\\\\)飞鸟娱乐\\[(www|bbs|hd).[a-zA-Z0-9]+.(com|net|cc|cn|org)\\][@. -]?(?=[^\\\\]+$)",
			"(?<=\\\\)梦幻天堂·龙网\\([a-zA-Z0-9]+.(com|net|cc|cn|org)\\).720p[@. -]?(?=[^\\\\]+$)",
			"(?<=\\\\)⊙(?=[^\\\\]+)",
			"(?<=\\\\)最新出品(?=[^\\\\]+)",
			"(?<=\\\\)【酷吧电影下载kuba222.com】(?=[^\\\\]+$)",
			"(?<=\\\\)2048社区 - (?=[^\\\\]+$)",
			"(?<=\\\\)[-_] +(?=[^\\\\]+$)",
			"(?<=\\\\)\\+[-_ ]*(?=[^\\\\]+$)",



			"(?<=\\\\)\\(Cosmo Planets VR\\)(?=[^\\\\]+$)",
			"(?<=\\\\)\\(CRYSTAL VR\\)(?=[^\\\\]+$)",
			"(?<=\\\\)\\(DEEP’S\\)(?=[^\\\\]+$)",
			"(?<=\\\\)\\(Global VR\\)(?=[^\\\\]+$)",
			"(?<=\\\\)\\(アリスJAPAN VR\\)(?=[^\\\\]+$)",
			"(?<=\\\\)\\(痴女ヘブン VR\\)(?=[^\\\\]+$)",
			
			
			"(?<=\\\\)\\[?(阳光)?电影(天堂)?[a-z]+\\.org[\\]\\.](?=[^\\\\]+$)",
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
			"(?<=\\\\)第一會所(新片)?(@SIS001)?@(?=[^\\\\]+$)",
			"(?<=\\\\)icao.me@(?=[^\\\\]+$)",
			"(?<=\\\\)2048社区 - \\w{1,3}2048.com@(84)(?=[^\\\\]+$)",
			"(?<=\\\\)JAVVR-Miyu Saito-84(?=[^\\\\]+$)",
			"(?<=\\\\)JAVVR-Nana Fukada-(?=[^\\\\]+$)",

			"(?<=\\\\)mhd1080.com@(?=[^\\\\]+$)",
//			"(?<=\\\\)[a-zA-Z0-9]+\\.com[- ._@](?=[^\\\\]+$)",
//			"(?<=\\\\)[a-zA-Z0-9]+\\.com[^a-zA-Z0-9](?=[^\\\\]+$)",

			"(?<=\\\\)VRPorn.com_(?=[^\\\\]+$)",
			

			"(?<=\\\\)84(?=KMVR-[^\\\\]+$)",
			"(?<=\\\\)84(?=kmvr-[^\\\\]+$)",

			"(?<=\\\\)(?!SLR_(Original|VR ))SLR_(?=[^\\\\]+$)", // SLR_VRMassage_Betzz_Cum.mp4 -> VRMassage_Betzz_Cum.mp4
			// SLR_VRedging.mp4 => VRedging.mp4
			"(?<=\\\\)SLR_(?=VRixxens_[^\\\\]+$)",
			"(?<=\\\\)SLR_(?=VRedging_[^\\\\]+$)",
			"(?<=\\\\)SLR_(?=perVRt_[^\\\\]+$)",
			"(?<=\\\\)SLR_(?=KinkyGirlsBerlin_[^\\\\]+$)",
			"(?<=\\\\)SLR_(?=Deepinsex_[^\\\\]+$)",
			"(?<=\\\\)SLR_(?=VRConk_[^\\\\]+$)",
			
			"(?<=\\\\)55(?=tmavr-[^\\\\]+$)",
			
			"(?<=\\\\)\\(kawaii VR\\)\\((?=[^\\\\]+$)",
			"(?<=\\\\)\\(S1 VR\\)\\((?=[^\\\\]+$)",
			"(?<=\\\\)\\(SSR VR\\)\\((?=[^\\\\]+$)",
			"(?<=\\\\)\\(こあらVR\\)\\((?=[^\\\\]+$)",
			"(?<=\\\\)\\(肉きゅんパラダイスVR\\)\\((?=[^\\\\]+$)",
			"(?<=\\\\)\\(Moodyz VR\\)\\((?=[^\\\\]+$)",
			"(?<=\\\\)\\(NATURAL HIGH VR\\)\\(1(?=[^\\\\]+$)",
			"(?<=\\\\)\\(ダスッ！ VR\\)\\((?=[^\\\\]+$)",
			"(?<=\\\\)\\(VRパラダイス\\)\\((?=[^\\\\]+$)",
			"(?<=\\\\)\\(Premium VR\\)\\((?=[^\\\\]+$)",
			"(?<=\\\\)\\(WANZ VR\\)\\((?=[^\\\\]+$)",
			"(?<=\\\\)\\(お夜食カンパニー\\)\\((?=[^\\\\]+$)",
			"(?<=\\\\)\\(レゾレボVR\\)\\((?=[^\\\\]+$)",
			
			
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
			"(?<=\\\\)www.98T.la@(?=[^\\\\]+$)",
			
			"(?<=\\\\)\\(kawaii\\)(?=[^\\\\]+$)",
			"(?<=\\\\SLR_)SLR (?=[^\\\\]+$)",

			"(?<=\\\\)1030xx.com-(?=[^\\\\]+$)",
			"(?<=\\\\)duopan.LA\\]-(?=[^\\\\]+$)",


			"(?<=\\\\)\\(Madonna\\)(?=[^\\\\]+$)",
			"(?<=\\\\)\\(CASANOVA\\)(?=[^\\\\]+$)",
			"(?<=\\\\)\\(S1\\)(?=[^\\\\]+$)",
			"(?<=\\\\)\\(KMP\\)(?=[^\\\\]+$)",
			"(?<=\\\\)\\(MOODYZ\\)(?=[^\\\\]+$)",
			"(?<=\\\\)\\(PRESTIGE ?VR\\)(?=[^\\\\]+$)",
			"(?<=\\\\)\\(V＆R PRODUCE\\)(?=[^\\\\]+$)",
			"(?<=\\\\)\\(KMP(VR)?\\)(?=[^\\\\]+$)",
			"(?<=\\\\)\\(DANDY(8K| )?VR\\)(?=[^\\\\]+$)",
			"(?<=\\\\)\\(ダスッ！VR\\)(?=[^\\\\]+$)",
			

			"(?<=\\\\.{1,30})-VR(?=\\.[^.\\\\]+$)",
			"(?<=\\\\.{1,88}).MP4-LUST(?=\\.[^.\\\\]+$)",
			"(?<=\\\\.{1,88}).MP4-Zsex3i(\\[rarbg\\])?(?=\\.[^.\\\\]+$)",
			

//			"(?<=\\\\.{1,99})_7680x3840(?=\\.mp4$)",
			"(?<=\\\\.{1,99})-r(?=\\.mp4$)",
			"(?<=\\\\.{1,99})\\.MP4-VACCiNE(\\[XC\\])?(?=\\.mp4$)",
			"(?<=\\\\.{1,99})\\(Oculus\\)(?=[^\\\\]+$)",
			 



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
			"(?<=\\\\.{1,90})_(?=_[^\\\\]+$)",


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
			"(?<=\\\\PS)[_ -](?=Porn[_ -\\.][^\\\\]+)",
			
			"(?<=\\\\)vrporn_(?=[^\\\\]+)",
			




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
			"^[a-z0-9]{1,7}-\\d{3,4}-\\d{1,3}(?=$)",
//			"^VRBangers(?=[-._ ][^\\\\]+$)",
//			"^swallowbay(?=[-._ ][^\\\\]+$)",
//			"^czechvrfetish(?=[-._ ][^\\\\]+$)",

			"dandy8kvr-\\d{3,4}(?=[-._ ][^\\\\]+$)",

			"^fc2[ _-]*ppv(?=[-._ ][^\\\\]+$)"


	};

	private static String[][] adReplaceRegs = new String [][] {
		{"(?<=\\\\)\\(?+([^(.]+)\\.[^(\\)]+\\)(?=[^\\\\]+$)", "$1-"}, // (vkrm1001.abcdef)new-artist.mp4 => vkrm1001-new-artist.mp4
		
//		{"(?<=\\\\\\w{1,9})0+(?=[1-9]\\d{0,9}[^\\\\]+$)", "-"},
		{"(?<=\\\\\\w{1,9}[a-z])0{2}(?=\\d{3}[^\\d][^\\\\]+$)", "-"},      // abc00123.mp4 => abc-123.mp4
		{"(?<=\\\\\\w{1,9}[a-z])0(?=\\d{2}[^\\d][^\\\\]+$)", "-0"},      // abc023.mp4 => abc-023.mp4

		{"([@ _-]+店長推薦作品[@ _-]+)", "-"},      // DSAM-35 店長推薦作品 Sec.mp4 => DSAM-35-Sec.mp4
		  
		
		{"(?<=\\\\\\w{1,9}[a-z])([1-9]\\d{3})(?=[a-z]\\.[^\\\\]+$)", "-$1-"},      // dsvr1234a.mp4 => dsvr-1234-A.mp4

		// the_suckubus_ORIGINAL_ENCODEDp_vrconk__180_lr.mp4
		{"(?<=\\\\)([^\\\\]*[^_\\\\]{1,99})(_{1,9}vrconk_{1,9})(?=[^\\\\]+$)", "VRConk_$1_"}, // the_suckubus_ORIGINAL_ENCODEDp_vrconk__180_lr.mp4 -> // VRConk_the_suckubus_ORIGINAL_ENCODEDp_180_lr.mp4
		{"(?<=\\\\)([^\\\\]*[^_\\\\]{1,99})(_{1,9}vrhard_{1,9})(?=[^\\\\]+$)", "VRHard_$1_"}, // the_suckubus_ORIGINAL_ENCODEDp_vrhard__180_lr.mp4 -> // VRHard_the_suckubus_ORIGINAL_ENCODEDp_180_lr.mp4
		{"(?<=\\\\)([^\\\\]*[^_\\\\]{1,99})(_{1,9}VRMansion_{1,9})(?=[^\\\\]+$)", "VRMansion_$1_"}, // the_suckubus_ORIGINAL_ENCODEDp_vrhard__180_lr.mp4 -> // VRHard_the_suckubus_ORIGINAL_ENCODEDp_180_lr.mp4

		{"(?<=\\\\)(VRConk.com)[ _-]{0,9}(?=[^\\\\]+$)", "VRConk_"},
		{"(?<=\\\\)(Plus\\.33)[ _\\.-]{0,9}(?=[^\\\\]+$)", "Plus33_"},
		
		{"(?<=\\\\)JVR(?=\\d+(\\.8K)?[^\\\\]+$)", "JVRPorn_"},  // JVR100184.8K.mp4 -> JVRPorn_100184.8K.mp4

		// correct case [capital]
		
		{"(?<=\\\\)(3DPickUp)[. _-]{1,9}(?=[^\\\\]+$)", "3DPickup_"}, // 3DPickUp_e.mp4 => 3DPickup_e.mp4
		{"(?<=\\\\)(virtualtaboo)[. _-]{1,9}(?=[^\\\\]+$)", "VirtualTaboo_"}, // virtualtaboo_e.mp4 => VirtualTaboo_e.mp4
		{"(?<=\\\\)(sinsvr)[. _-]{1,9}(?=[^\\\\]+$)", "SinsVR_"}, // sinsvr_e.mp4 => SinsVR_e.mp4
		{"(?<=\\\\)(vrpornjack)[. _-]{1,9}(?=[^\\\\]+$)", "VRPornJack_"}, // vrpornjack_e.mp4 => VRPornJack_e.mp4
		{"(?<=\\\\)(no2studiovr)[. _-]{1,9}(?=[^\\\\]+$)", "No2StudioVR_"}, // no2studiovr_e.mp4 => No2StudioVR_e.mp4
		{"(?<=\\\\)(vrvr)[. _-]{1,9}(?=[^\\\\]+$)", "VRVR-"}, // vrvr_108.mp4 => VRVR_108.mp4
		{"(?<=\\\\)(VRCONK|vrconk)[. _-]{1,9}(?=[^\\\\]+$)", "VRConk-"}, // VRCONK-108.mp4 => VRConk-108.mp4
		{"(?<=\\\\)(VRHARD|vrhard)[. _-]{1,9}(?=[^\\\\]+$)", "VRHard-"}, // VRHARD-108.mp4 => VRHard-108.mp4
		{"(?<=\\\\)(VRmodels|vrmodels)[. _-]{1,9}(?=[^\\\\]+$)", "VRModels-"}, // VRmodels-108.mp4 => VRModels-108.mp4
		{"(?<=\\\\)(PsPorn|psporn)[. _-]{1,9}(?=[^\\\\]+$)", "PSPorn-"}, // PsPorn-108.mp4 => PSPorn-108.mp4
		{"(?<=\\\\)(VRcosplayx|vrcosplayx)[. _-]{1,9}(?=[^\\\\]+$)", "VRCosplayX-"}, // vrcosplayx-108.mp4 => VRCosplayX-108.mp4
		{"(?<=\\\\)(Squeezevr|Squeeze VR)[. _-]{1,9}(?=[^\\\\]+$)", "SqueezeVR-"}, // Squeezevr-108.mp4 => SqueezeVR-108.mp4
		{"(?<=\\\\)(realjamvr)([. _-]{1,9})(?=[^\\\\]+$)", "RealJamVR-"}, // realjamvr-108.mp4 => RealJamVR-108.mp4
//		{"(?<=\\\\)(vredging)([. _-]{1,9})(?=[^\\\\]+$)", "VREdging-"}, // vredging-108.mp4 => VREdging-108.mp4
		{"(?<=\\\\)(jimmydraws)([. _-]{1,9})(?=[^\\\\]+$)", "JimmyDraws-"}, // jimmydraws-108.mp4 => JimmyDraws-108.mp4
		{"(?<=\\\\)(lethal-?hardcore-?vr)([. _-]{1,9})(?=[^\\\\]+$)", "LethalHardcoreVR-"}, // lethalhardcorevr-108.mp4 => LethalHardcoreVR-108.mp4
		{"(?<=\\\\)(noir)([. _-]{1,9})(?=[^\\\\]+$)", "Noir-"}, // noir-108.mp4 => Noir-108.mp4
		{"(?<=\\\\)(tadpolexxxstudio)([. _-]{1,9})(?=[^\\\\]+$)", "TadPoleXXXStudio-"}, // tadpolexxxstudio-108.mp4 => TadPoleXXXStudio-108.mp4
		{"(?<=\\\\)(vrlatina)([. _-]{1,9})(?=[^\\\\]+$)", "VRLatina-"}, // vrlatina-108.mp4 => VRLatina-108.mp4
		{"(?<=\\\\)(covertjapan)([. _-]{1,9})(?=[^\\\\]+$)", "CovertJapan-"}, // covertjapan-108.mp4 => CovertJapan-108.mp4
		{"(?<=\\\\)(czechvrcasting)([. _-]{1,9})(?=[^\\\\]+$)", "CzechVRCasting-"}, // covertjapan-108.mp4 => CovertJapan-108.mp4
		{"(?<=\\\\)(pervrt)([. _-]{1,9})(?=[^\\\\]+$)", "perVRt-"}, // pervrt-108.mp4 => perVRt-108.mp4
		{"(?<=\\\\)(SWALLOWBAY)([. _-]{1,9})(?=[^\\\\]+$)", "SwallowBay-"}, // SWALLOWBAY_-108.mp4 => SwallowBay-108.mp4
		{"(?<=\\\\)(virtualrealporn)([. _-]{1,9}\\.com)(?=[^\\\\]+$)", "VirtualRealPorn-"}, // virtualrealporn-108.mp4 => VirtualRealPorn-108.mp4
		{"(?<=\\\\)(vrallure)([. _-]{1,9})(?=[^\\\\]+$)", "VRAllure-"}, // vrallure-108.mp4 => VRAllure-108.mp4
		{"(?<=\\\\)(vroomed)([. _-]{1,9})(?=[^\\\\]+$)", "VRoomed-"}, // vroomed-108.mp4 => VRoomed-108.mp4
		{"(?<=\\\\)(slr_originals)([. _-]{1,9})(?=[^\\\\]+$)", "SLR_Originals-"}, // slr_originals_wakeup_call.mp4 => SLR_Originals-wakeup_call.mp4
//		{"(?<=\\\\)(vrpornjack)([. _-]{1,9})(?=[^\\\\]+$)", "VRPornJack-"}, // vrpornjack-108.mp4 => VRPornJack-108.mp4
		{"(?<=\\\\)(wankzvr)([. _-]{1,9})(?=[^\\\\]+$)", "WankzVR-"}, // wankzvr-108.mp4 => WankzVR-108.mp4
		{"(?<=\\\\)(czechvrfetish)([. _-]{1,9})(?=[^\\\\]+$)", "CzechVRFetish-"}, // czechvrfetish-108.mp4 => CzechVRFetish-108.mp4
		{"(?<=\\\\)(kinkvr)([. _-]{1,9})(?=[^\\\\]+$)", "KinkVR-"}, // kinkvr-108.mp4 => KinkVR-108.mp4
		{"(?<=\\\\)(milfvr)([. _-]{1,9})(?=[^\\\\]+$)", "MilfVR-"}, // milfvr-108.mp4 => MilfVR-108.mp4
		{"(?<=\\\\)(xvirtual)([. _-]{1,9})(?=[^\\\\]+$)", "XVirtual-"}, // xvirtual-108.mp4 => XVirtual-108.mp4
		{"(?<=\\\\)(POVcentralVR)([. _-]{1,9})(?=[^\\\\]+$)", "POVCentralVR-"}, // POVcentralVR-108.mp4 => POVCentralVR-108.mp4
		{"(?<=\\\\)(povcentralvr)([. _-]{1,9})(?=[^\\\\]+$)", "POVCentralVR-"}, // povcentralvr-108.mp4 => POVCentralVR-108.mp4
		{"(?<=\\\\)(POVR\\.originals)([. _-]{1,9})(?=[^\\\\]+$)", "POVROriginals-"}, // POVR originals-108.mp4 => POVROriginals-108.mp4
		{"(?<=\\\\)(POVR -)([. _-]{1,9})(?=[^\\\\]+$)", "POVCentralVR-"}, // POVR - 108.mp4 => POVCentralVR-108.mp4
		{"(?<=\\\\)(VRBANGERS)([. _-]{1,9})(?=[^\\\\]+$)", "VRBangers-"}, // VRBANGERS-108.mp4 => VRBangers-108.mp4
		{"(?<=\\\\)(vrpornnow|VRPornnow)([. _-]{1,9})(?=[^\\\\]+$)", "VRPornNow-"}, // vrpornnow-108.mp4 => VRPornNow-108.mp4
		{"(?<=\\\\)(peepingthom)([. _-]{1,9})(?=[^\\\\]+$)", "PeepingThom-"}, // peepingthom-108.mp4 => PeepingThom-108.mp4
		{"(?<=\\\\)(Peeping_Thom)([. _-]{1,9})(?=[^\\\\]+$)", "PeepingThom-"}, // Peeping_Thom-108.mp4 => PeepingThom-108.mp4
		{"(?<=\\\\)(BadoinkVR)([. _-]{1,9})(?=[^\\\\]+$)", "BaDoinkVR-"}, // BadoinkVR-108.mp4 => BaDoinkVR-108.mp4
		{"(?<=\\\\)(deepinsex)([. _-]{1,9})(?=[^\\\\]+$)", "Deepinsex-"}, // deepinsex-108.mp4 => Deepinsex-108.mp4
		{"(?<=\\\\)(DeepInSex)([. _-]{1,9})(?=[^\\\\]+$)", "Deepinsex-"}, // DeepInSex-108.mp4 => Deepinsex-108.mp4
		{"(?<=\\\\)(TmwVRNet)([. _-]{1,9})(?=[^\\\\]+$)", "TmwVRnet-"}, // TmwVRNet-108.mp4 => TmwVRnet-108.mp4
		{"(?<=\\\\)(sexbabesvr)([. _-]{1,9})(?=[^\\\\]+$)", "SexBabesVR-"}, // stockingsvr-108.mp4 => SexBabesVR-108.mp4
		{"(?<=\\\\)(stockingsvr)([. _-]{1,9})(?=[^\\\\]+$)", "StockingsVR-"}, // stockingsvr-108.mp4 => StockingsVR-108.mp4
		{"(?<=\\\\)(VREdging)([. _-]{1,9})(?=[^\\\\]+$)", "VRedging-"}, // VREdging-108.mp4 => VRedging-108.mp4
		{"(?<=\\\\)(VRsolos)([. _-]{1,9})(?=[^\\\\]+$)", "VRSolos-"}, // VRsolos-108.mp4 => VRSolos-108.mp4
//		{"(?<=\\\\)(povcentralvr)([. _-]{1,9})(?=[^\\\\]+$)", "POVCentralVR-"}, // povcentralvr-108.mp4 => POVCentralVR-108.mp4
//		{"(?<=\\\\)(povcentralvr)([. _-]{1,9})(?=[^\\\\]+$)", "POVCentralVR-"}, // povcentralvr-108.mp4 => POVCentralVR-108.mp4
//		{"(?<=\\\\)(povcentralvr)([. _-]{1,9})(?=[^\\\\]+$)", "POVCentralVR-"}, // povcentralvr-108.mp4 => POVCentralVR-108.mp4
		
		
		
		{"(?<=\\\\)(vrixxens)([. _-]{1,9})(?=[^\\\\]+$)", "VRixxens$2"}, // vrixxens-108.mp4 => VRixxens-108.mp4
		{"(?<=\\\\)(Taboo[ _-]VR[ _-]Porn)([. _-]{1,9})(?=[^\\\\]+$)", "TabooVRPorn-"}, // Taboo VR Porn-108.mp4 => TabooVRPorn-108.mp4
		
		{"(?<=\\\\)(dsam)([. _-]{1,9})(?=[^\\\\]+$)", "DSAM-"}, // dsam-108.mp4 => DSAM-108.mp4
		
		{"(?<=\\\\[a-zA-z0-9]{3,9}-\\d{3,5})vrv1uhq[ef](?=\\d{1,2}[^\\\\]+$)", "-"}, // juvr-165vrv1uhqe1.mp4 => juvr-165-1.mp4
		
		{"(?<=\\\\)(\\[VRBangers.com\\])[ _-]{0,9}(?=[^\\\\]+$)", "VRBangers_"},
		{"(?<=\\\\VirtualRealPorn)\\.com[ _-]{0,9}(?=[^\\\\]+$)", "_"},
//		{"(?<=\\\\SinsVR)\\.com\\](?=[ _-]{0,9}[^\\\\]+$)", ""},
		
		

		
		{"(?<=\\\\)(\\[VRCosplayX.com\\])[ _-]{0,9}(?=[^\\\\]+$)", "VRCosplayX_"}, // [VRCosplayX.com] Hogwarts Legacy.mp4 -> VRCosplayX_Hogwarts Legacy.mp4
		{"(?<=\\\\)(\\[VRConk.com\\])[ _-]{0,9}(?=[^\\\\]+$)", "VRConk_"}, // [VRConk.com] Hogwarts Legacy.mp4 -> VRConk_Hogwarts Legacy.mp4


		{"(?<=\\\\)(\\[POVR.com;POVROriginals(.com)?\\])[ _-]{0,9}(?=[^\\\\]+$)", "POVROriginals_"},

		{"(?<=\\\\.{1,150})\\.part(\\d+)\\s+(?=[^\\\\]+$)", "-$1"}, // HUNVR-107.part1 - 7 Babes Fucked In A Share House.mp4 => HUNVR-107-1- 7 Babes Fucked In A Share House.mp4
		
		{"(?<=\\\\.{1,150})\\.part(\\d+)(?=_[1-9][0,9]{0,1}k\\.[^.\\\\]+$)", "-$1"}, // AJVR-203.part1_8K.mp4 => AJVR-203-1_8K.mp4

		{"(?<=\\\\[a-z0-9]{1,7}-\\d{3,4})[-._ ]p(\\d+)\\s+(?=[^\\\\]+$)", "-$1-"}, // MDVR-092 P2 HITOMI H265_2048p_180_LR_3dh.mp4 => MDVR-092-2-HITOMI H265_2048p_180_LR_3dh.mp4

		{"(?<=\\\\.{1,99})_4K-(?=[^\\\\]+$)", "-"},
		{"(?<=\\\\.{1,99}) \\[HQ-VR\\] (?=[^\\\\]+$)", "-"},
		{"(?<=\\\\.{1,99}\\d{3,5})hhb(?=\\d{1,}[^\\\\]+$)", "-"},
		{"(?<=\\\\.{1,99}-)000(?=\\d{1,}[^\\\\]+$)", "0"},
		{"(?<=\\\\.{1,150})(\\.|-)(part|pt|R|CD)(\\d{1,2})(?=\\.[^\\\\]+$)", "-$3"},
		{"(?<=\\\\.{1,150})\\s{1,99}part\\s{1,99}(\\d{1,2})(?=\\.[^\\\\]+$)", "-$1"},
		
//		{"(?<=\\\\.{1,150}) DSVR-1034 (?=[^\\\\]+$)", "-"},
//		{"(?<=\\\\.{1,150})-4K-(?=[^\\\\]+$)", "-"},
		{"(?<=\\\\)SLR ?Originals(?=[-_ ][^\\\\]+$)", "SLR_Originals"}, // SLROriginals-1.mp4 => SLR_Originals-1.mp4
//		{"(?<=\\\\)SLR Originals(?=[ -_][^\\\\]+$)", "SLR_Originals"}, // SLR Originals-1.mp4 => SLR_Originals-1.mp4
		{"(?<=\\\\)SLR_VR Massage(?=[-_ ][^\\\\]+$)", "VRMassage"}, // SLR_VR Massage_Josephine.mp4 => VRMassage_Josephine.mp4
		{"(?<=\\\\)SLR_VR Pornnow(?=[-_ ][^\\\\]+$)", "VRPornoNow"}, // SLR_VR Massage_Josephine.mp4 => VRPornoNow_Josephine.mp4
		
		
		{"(?<=\\\\)SexLikeReal(?=[ _.-][^\\\\]+$)", "SLR"},
		{"(?<=\\\\)All Anal VR(?=[ _.-][^\\\\]+$)", "AllAnalVR"},
		
		
		 


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

		{"(?<=\\\\[a-zA-Z0-9]{2,7}-\\d{3,4}) (?=\\d{1,2}\\.[^\\\\]+$)", "-"},  //  abc-098 1.mp4  => abc-1.mp4
		{"(?<=\\\\[a-zA-Z0-9]{2,7}-\\d{3,4})([a-z])(?=\\.[^\\\\]+$)", "-$1"},  //  abc-098a.mp4  => abc098-a.mp4

		{"(?<=\\\\[a-zA-Z0-9]{2,7}-\\d{3,4})[-_ ]R(\\d{1,2})(?=\\.[^\\\\]+$)", "-$1"},  //  3DSVR-1313_R2.mp4  => 3DSVR-1313-2.mp4
		
		{"(?<=\\\\[a-zA-Z0-9]{2,7}-\\d{3,4}) ([1-9]{1,2}k) ([^\\\\]{1,299})(?=\\.[^.\\\\]+$)", "-$2_$1"},  //  SAVR-272 8K Momo Honda-1.mp4  => SAVR-272_Momo Honda-1_8K.mp4
		

		{"(?<=\\\\)(\\[日本剧情\\])(.*)(?=\\.[^.\\\\]+$)", "$2$1"},
		{"(?<=\\\\)(\\[日本\\])(.*)(?=\\.[^.\\\\]+$)", "$2$1"},
		{"(?<=\\\\)(\\[韩国三级\\])(.*)(?=\\.[^.\\\\]+$)", "$2$1"},
		
		// at_the_masage_parlor__wild_experiences_ORIGINAL_ENCODEDp_vrpornjack__180_lr.mp4 => vrpornjack_at_the_masage_parlor__wild_experiences_180_lr.mp4
		// at_the_bar_pay_the_bartender_ORIGINAL_ENCODED_p_vrpornjack__180_lr.mp4 => vrpornjack_at_the_bar_pay_the_bartender_180_lr.mp4
		{"(?<=\\\\)([^\\\\]+)_ORIGINAL_ENCODED_?p_([a-zA-Z0-9]{3,20})[-_ ]{1,5}?(?=180[^\\\\]+$)", "$2_$1_"},
		
		
//		{"(?<=\\\\)([^\\\\]+)(_original_p_VRPornJack_)(?=[^\\\\]+$)", "VRPornJack_$1_"}, // Webmodel_Exposure_Alexa_Sky_6K_original_p_VRPornJack_180_lr.mp4 => VRPornJack_Webmodel_Exposure_Alexa_Sky_6K_180_lr.mp4
		{"(?<=\\\\)([^\\\\]+)_original_p_(?!vrporncom_)([a-zA-Z0-9]{4,20})_(video_)?(?=[^\\\\]+$)", "$2_$1_"}, // Strap-on_Review_Rosse_Ariana_Ayana_6K_original_p_No2StudioVR_video_180_LR.mp4 => No2StudioVR_Strap-on_Review_Rosse_Ariana_Ayana_6K_original_180_LR.mp4
		{"(?<=\\\\)([^\\\\]+)_original_([a-zA-Z0-9]{4,20})_p_(video_)?(?=[^\\\\]+$)", "$2_$1_"}, // experience-travelers_8K_original_virtualrealporn_p_video_180_lr.mp4 => virtualrealporn_experience-travelers_8K_original_video_180_lr.mp4

		
		
		// 144-czechvrcasting-3d-5400x2700-60fps-oculusrift_hq_h265.mp4 => czechvrcasting.E144.5400x2700-60fps-oculusrift_hq_h265.mp4
		{"(?<=\\\\)(\\d{3,4})-(czechvrcasting)-3d-(?=[^\\\\]+$)", "$2.E$1."},
		{"(?<=\\\\)(\\d{3,4})-(czechvrfetish)-3d-(?=[^\\\\]+$)", "$2.E$1."},
		{"(?<=\\\\)(\\d{3,4})-(czechvr)-3d-(?=[^\\\\]+$)", "$2.E$1."},
		{"(?<=\\\\)(\\d{3,4})-(vrintimacy)-3d-(?=[^\\\\]+$)", "VRIntimacy-E$1-"},
		
		

		{"(?<=\\\\)(DSVR-)(.*)(?=\\.[^.\\\\]+$)", "3$1$2"},   // DSVR-001-1 -> 3DSVR-001-1

		{"(?<=\\\\)1(3dsvr)(?=.*\\.[^.\\\\]+$)", "3DSVR"},   // DSVR-001-1 -> 3DSVR-001-1

		{"(?<=\\\\)3dsvr-(\\d{3})-(?=[^\\\\]+\\.[^.\\\\]+$)", "3DSVR-0$1-"},    // 3dsvr-123-1.mp4   => 3dsvr-0123-1.mp4
		{"(?<=\\\\)3(dsvr|DSVR)0(\\d{4})-(?=[^\\\\]+\\.[^.\\\\]+$)", "3DSVR-$2-"},     // 3dsvr01234-1.mp4 => 3dsvr-1234-1.mp4
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
		{"(?<=\\\\)(vac-bdvr)(?=\\d{6}[^\\\\]+\\.[^.\\\\]+$)", "BaDoinkVR-$1"},
		{"(?<=\\\\)(vac-slr)(?=\\d{6}[^\\\\]+\\.[^.\\\\]+$)", "SLR-$1"},
		{"(?<=\\\\)(vac-zvr)(?=\\d{6}[^\\\\]+\\.[^.\\\\]+$)", "ZEXYVR-$1"},
		{"(?<=\\\\)(vac-drvr)(?=\\d{6}[^\\\\]+\\.[^.\\\\]+$)", "DarkRoomVR-$1"},
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
		{"(?<=\\\\)(MyGirlfriendsBustyFriend\\.)(?=[^\\\\]+$)", "NaughtyAmerica-$1"},
		{"(?<=\\\\)(MyNaughtyMassage\\.)(?=[^\\\\]+$)", "NaughtyAmerica-$1"},
		{"(?<=\\\\)(MySistersHotFriend\\.)(?=[^\\\\]+$)", "NaughtyAmerica-$1"},
		{"(?<=\\\\)(Classroom\\.)(?=[^\\\\]+$)", "NaughtyAmerica-$1"},
		{"(?<=\\\\)(NeighborAffair\\.)(?=[^\\\\]+$)", "NaughtyAmerica-$1"},
		{"(?<=\\\\)(PerfectFuckingStrangers\\.)(?=[^\\\\]+$)", "NaughtyAmerica-$1"},
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
//			newAbsPath = ComRegexUtil.replaceByRegexIGroup(oriAbsPath, currentReg, currentReplacement);
			newAbsPath = ComRegexUtil.replaceByRegexGroup(oriAbsPath, currentReg, currentReplacement);

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
		File[] nextFolderArr = new File[1];
		nextFolderArr[0] = dir;
		doOneLevel(nextFolderArr);
	}
	
	private static void doOneLevel(File[] dirs) throws Exception {
		List<File> files = AdsVideoRm.unionDirs(dirs);
        int length = files.size();
		List<File> folders = new ArrayList<File>();
		for(int i = 0; i < length; i++) {
			File file = files.get(i);
			File originFile = file;
			String getAbsolutePath = file.getPath();
			String nameOnly = file.getName();
//			ComLogUtil.info("file1:" + file.getAbsolutePath());
//			ComLogUtil.info("file2:" + file.getName());
//			ComLogUtil.info("file3:" + file.getPath());

			if(file.isDirectory()) {
				if(ComFileUtil.getFileName(originFile, true).endsWith("_KEEP")) {
					// skip for KEEP folder
				} else {
					folders.add(file);
				}
			} else {
				if(ComFileUtil.getFileName(originFile, false).endsWith("_KEEP")) {
					// skip for KEEP file
				} else {
					if(originFile == file) file = removeAdsPrefix(file);
					if(originFile == file) file = removePreSuffSpace(file);
					if(originFile == file) file = replaceAds(file);
//				if(originFile == file) removeAdsPrefix(file);
//				if(originFile == file) removePreSuffSpace(file);
					if(originFile == file) file = UppercaseVideoID(file);
					if(originFile == file) file = AppendVideoResolution(file, appendResultionIfFirstNumberGreaterThan);
//				if(originFile == file) file = AppendVideoDuration(file);
				}
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
		return AppendVideoResolution(file, 1);
	}
	
	/**
	 * a.mp4 => a_4096x2048.mp4
	 * @param file the File to append video resolution to
	 * @param appendResultionIfFirstNumberGreaterThan when '5', then 4096x2048 video won't append resolution, but 8192x4096 will append resolution.
	 * @return the new File
	 * @throws Exception
	 */
	private static File AppendVideoResolution(File file, int appendResultionIfFirstNumberGreaterThan) throws Exception {
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
				ComRegexUtil.test(fileNameOnly, "[_ -.](?!180x180)\\d{3,4}x\\d{3,4}([_ -.]|$)") // PVRStudio_5760x2880_.mp4 PVRStudio_5760x2880.mp4
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
			if(videoResolution.toString().length() == 0) {
				throw new Exception("videoResolution length invalid - videoResolution:" + videoResolution);
			}
			int firstNumber = Integer.parseInt(videoResolution.toString().charAt(0) + "", 10);
			if(firstNumber > appendResultionIfFirstNumberGreaterThan) {
				fileName.append("_" + videoResolution);
				ret = fileName.toFile();
				ComFileUtil.doRename(!isPrintOnly, file, ret, "by videoSolution");
			} else {
				ComLogUtil.info("won't append resolution for :" + fileNameOnly + ", resolution: " + videoResolution);
			}
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

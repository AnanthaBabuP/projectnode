package jp.co.wl.service.wbms05.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.wl.base.common.util.Constants;
import jp.co.wl.base.common.util.StringUtil;
import jp.co.wl.dto.wbms05.Wbms0510DTO;
import jp.co.wl.entity.common.ComboItem;
import jp.co.wl.entity.wbms00.Wbms0010UniversalParam;
import jp.co.wl.base.common.util.DateTimeUtil;
import jp.co.wl.base.common.util.WbmsConstants;
import jp.co.wl.dto.wbms05.Wbms0510InitResponseDto;
import jp.co.wl.dto.wbms05.Wbms0510PrintConditionRequestDto;
import jp.co.wl.dto.wbms05.Wbms0510PrintDateRequestDto;
import jp.co.wl.dto.wbms05.Wbms0510SearchConditionDto;
import jp.co.wl.entity.common.WbmsFLog;
import jp.co.wl.entity.wbms05.Wbms0510PrintPullOutInfo;
import jp.co.wl.repository.common.WbmsFLogRepository;
import jp.co.wl.repository.wbms05.Wbms0510Repository;
import jp.co.wl.request.wbms05.Wbms0510PrintRequest;
import jp.co.wl.request.wbms05.Wbms0510Request;
import jp.co.wl.request.wbms05.Wbms0510RequestList;
import jp.co.wl.response.wbms05.Wbms0510InitResponse;
import jp.co.wl.response.wbms05.Wbms0510Response;
import jp.co.wl.response.wbms05.Wbms0510ResultResponse;
import jp.co.wl.service.wbms05.Wbms0510Service;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

@Service
public class Wbms0510ServiceImpl implements Wbms0510Service {

    @Autowired
    private Wbms0510Repository wbms0510Repository;
    
   // @Autowired
    private WbmsFLogRepository wbmsFLogRepository;

    //@Autowired
    //private Wbms0510DTO wbms0510DTO;

    @Autowired
    private Wbms0510Request wbms0510Request;

    //@Autowired
    //private WbmsMGyoScheduleRepository wbmsMGyoScheduleRepository;

    /**
     * 初期表示処理
     */
    public Wbms0510InitResponse init() {

        // 検針地区マスタのデータ取得
        List<Wbms0510InitResponseDto> kenChikuData = wbms0510Repository.getKenChikuData();

        // 検針地区マスタのデータ取得
        List<Wbms0510InitResponseDto> masKbnData = wbms0510Repository.getMasKbnData();

        // WLコントロールマスタ
        Wbms0510InitResponseDto wbmsMCtrInfoDataset = wbms0510Repository.getMasCtrData();

        // 詰め替え処理
        Wbms0510InitResponse resultList = new Wbms0510InitResponse();

        resultList.setErrorCode("");
        resultList.setErrorMsg("");
        resultList.setPaymentDivDataset(masKbnData);
        resultList.setMeterReadingDistrictDataset(kenChikuData);
        resultList.setWbmsMCtrInfoDataset(wbmsMCtrInfoDataset);

        return resultList;
    }

    /***
	 * @param Wbms0510ListPrintRequest wbms0510PrintListRequest
	 * @return JasperPrint
	 */
	public JasperPrint executePrintExtractionList(Wbms0510PrintRequest wbms0510PrintRequest) {
		
		JasperPrint empReport = null;
        JasperReport jasperReport = null;
		try {
		//TODO開始
			// List<SessionInfo> sessionInfoList = sessionService.getSessionInfoList();
			// SessionInfo sessionInfo = sessionInfoList.get(0);
			// 事業体コード
		    //String companyCd = sessionInfo.getCompanyCd();
		    String companyCd = "000001";
			
            Timestamp timestamp = DateTimeUtil.getCurrentTimestamp();
            Wbms0510PrintConditionRequestDto printCondition = wbms0510PrintRequest.getPrintConditionDto();
            Wbms0510PrintDateRequestDto printScheduleData = wbms0510PrintRequest.getPrintDateDto();
			Wbms0510SearchConditionDto printSearchParams = new Wbms0510SearchConditionDto();
			// printSearchParams.setCorporationCd(companyCd);
			// printSearchParams.setAdjustment_ym_start(listSearch.getAdjustmentYmStart());
			// printSearchParams.setAdjustment_ym_end(listSearch.getAdjustmentYmEnd());
			// printSearchParams.setWater_faucet_no(listSearch.getWaterFaucetNo());
			// printSearchParams.setDelinquency_status_div(listSearch.getDelinquencyStatusDiv());
			// printSearchParams.setId(listSearch.getId());
			// printSearchParams.setDelinquency_status(listSearch.getDelinquencyStatus());
			
			List<Wbms0510PrintPullOutInfo> listResult = wbms0510Repository
					.executeExtractionList(printSearchParams);

			// レポートに必要な動的パラメータ
			Map<String, Object> empParams = new HashMap<String, Object>();
			empParams.put("Wbn,", "SysDate");
			JRDataSource  jrDataSource =new JRBeanCollectionDataSource(listResult);
			String path="C:/waterLink/src/branch/waterlinks/api/src/main/resources/wbms051002.jrxml";
			jasperReport = JasperCompileManager.compileReport(path);
			// jasperReport = JasperCompileManager.compileReport("classpath*/wbms051002.jrxml");
		     empReport = JasperFillManager.fillReport(jasperReport // jasper レポートのパス
					, empParams // 動的パラメータ
				, jrDataSource);

			// ログファイルを更新処理実施
			this.insertExtractionListLogData(timestamp, wbms0510PrintRequest);
            //TODO終了
		} catch (JRException e) {
			e.printStackTrace();
		}
		return empReport;
	}

    /**
     * 全件取得処理
     */
    public Wbms0510Response execute(Wbms0510RequestList wbms0510RequestList) {
        // セッション情報取得
//        List<SessionInfo> sessionInfoList = sessionService.getSessionInfoList();
//        SessionInfo sessionInfo = sessionInfoList.get(0);
        // 4.検索パラメータの設定を行う
        Wbms0510DTO wbms0510DTO = this.setSearchCriteria(wbms0510RequestList, wbms0510Request.getPaymentDivDataset());
        
        // データ取得
        List<Wbms0510ResultResponse> searchResult = this.search(wbms0510DTO);

        // 詰め替え処理
        List<Wbms0510ResultResponse> resData = new ArrayList<Wbms0510ResultResponse>();
        // 詰め替え処理
        Wbms0510Response resultList = new Wbms0510Response();

        // 検索結果詰替え
        if (wbms0510RequestList.getUniversal() != null) {
            // ユニバーサルが入力されていた場合、検索結果をそのまま詰替える
            resData = this.getSearchUniversal(searchResult, wbms0510RequestList.getSearchResultMaxCount());
        } else {
            // ユニバーサルが入力されていない場合、画面.未納件数以上のデータのみ詰替える
            resData = this.getSearch(searchResult, wbms0510RequestList.getUnreceiptCount(), wbms0510RequestList.getSearchResultMaxCount());
        }

        // データ件数を取得
//        String resultSize = String.valueOf(resData.size());
//        resultInfo.setResult_size(resultSize);
//        resultInfoList.add(resultInfo);

        //返却用変数に格納
//        result.setWbms0510ResultResponse(resData);
//        result.setResultInfo(resultInfoList);
//        result.setWbms0510ScheduleDateDateset(getScheduleDate(wbms0510RequestList));
        resultList.setWbms0510ListDb(resData);
        return resultList;
    }

    /**
     * データ取得.
     * @param wbms0510Form 検索条件
     * @return 検索結果
     */
    private List<Wbms0510ResultResponse> search(Wbms0510DTO param) {

        // ビューよりデータ取得
        return wbms0510Repository.execute(param);
    }

    /**
     * 検索結果の未納件数を取得.
     * @param searchResultList  検索結果リスト
     * @param waterFaucetNo     水栓番号
     * @param historyNo         歴番
     * @return 未納件数
     */
    private int getUnreceiptCount(List<Wbms0510ResultResponse> searchResultList, String waterFaucetNo, String historyNo) {

        // 未納件数返却用変数
        int unreceiptCount = 0;

        for (Wbms0510ResultResponse data : searchResultList) {
            
            // 水栓番号と歴番が引数のものと同じレコード件数をカウント
            if (waterFaucetNo.equals(data.getWaterFaucetNo()) && historyNo.equals(historyNo)) {
                unreceiptCount++;
            }
        }

        return unreceiptCount;
    }

    /**
     * 西暦フォーマット変換処理(年月)
     * 
     * @param dateStr
     *            年月
     * @return 西暦
     */
    private String convertStrYmToStrAlphaWesternCalendar(String dateStr) {
        try {
//            return DateTimeUtil.convertStrYmToStrAlphaWesternCalendar(dateStr);
            return "";
        } catch (Exception ex) {
            return "";
        }
    }
//         /**
//      * 業務日程マスタ検索を実行します。
//      * @param searchReq リクエスト
//      * @return ResWbms0510Search レスポンス
//      */
//     public List<Wbms0510ScheduleDateSearchResult> getScheduleDate(Wbms0510RequestList param) {
//         // 返却用変数
//         Wbms0510ScheduleDateSearchResult searchResult = new Wbms0510ScheduleDateSearchResult();
//         List<Wbms0510ScheduleDateSearchResult> scheduleDateDataset = new ArrayList<Wbms0510ScheduleDateSearchResult>();

//         // 検索用変数
//         WbmsMGyoSchedulePK wbmsMGyoSchedulePK = new WbmsMGyoSchedulePK();
//         // セッション情報取得
// //        List<SessionInfo> sessionInfoList = sessionService.getSessionInfoList();
// //        SessionInfo sessionInfo = sessionInfoList.get(0);

// //        wbmsMGyoSchedulePK.setCorporationCd(sessionInfo.getCompanyCd());       // 事業所コード
//         wbmsMGyoSchedulePK.setAdjustmentYm(param.getAdjustmentYmStart());  // 調定年月

//         // 検索を実行
//         WbmsMGyoSchedule wbmsMGyoSchedule = wbmsMGyoScheduleRepository.findOne(wbmsMGyoSchedulePK);

//         // データの詰め替えを行う
//         if (null != wbmsMGyoSchedule) {
//             searchResult.setIssue_date(wbmsMGyoSchedule.getDemandSendingDate());  // 督促発送日付
//             searchResult.setPayment_deadline(wbmsMGyoSchedule.getDemandPaymentDeadline());    // 督促納付期限
//         }

//         // 返却地を設定
//         scheduleDateDataset.add(searchResult);
//         return scheduleDateDataset;

//     }

/**
     * ユニバーサル入力無しの検索処理
     * @param dataList              検索結果リスト
     * @param unpaidCount           未納件数
     * @param searchResultMaxCount  最大検索結果取得件数
     * @return
     */
    private List<Wbms0510ResultResponse> getSearch(List<Wbms0510ResultResponse> dataList, String unpaidCount,int searchResultMaxCount) {

        // 詰め替え処理
        List<Wbms0510ResultResponse> resData = new ArrayList<Wbms0510ResultResponse>();

        // セッション情報取得
//        List<SessionInfo> sessionInfoList = sessionService.getSessionInfoList();
//        SessionInfo sessionInfo = sessionInfoList.get(0);

        // 未納件数カウント用変数
        int unreceiptCount = 1;
        // 未納件数カウント用一つ前のレコード水栓番号
        String beforeWaterFaucetNo = "";
        // 未納件数カウント用一つ前のレコード歴番
        String beforeHistoryNo = "";
        // 未納件数カウント用現在レコード歴番
        String nowWaterFaucetNo = "";
        // 未納件数カウント用現在レコード水栓番号
        String nowHistoryNo = "";

        // 返却用変数に検索結果を格納
        // 検索データ総件数カウント用兼レコード採番用変数
        int dataCount = 0;
        // レコードの採番用変数
        //int count = 0;
        for (Wbms0510ResultResponse data : dataList) {

            // 現在レコードの水栓番号と歴番を取得
            nowWaterFaucetNo = data.getWaterFaucetNo();
            nowHistoryNo = data.getHistoryNo();

            // 取得した水栓番号と歴番が直前のレコードと異なる場合、未納件数をカウントする。
            if (!(beforeWaterFaucetNo.equals(nowWaterFaucetNo) && beforeHistoryNo.equals(nowHistoryNo)) ) {
                unreceiptCount = this.getUnreceiptCount(dataList, nowWaterFaucetNo, nowHistoryNo);
            }

            // 画面入力.未納件数以上の時、返却用変数に検索結果を格納
            if (unreceiptCount >= Integer.parseInt(unpaidCount)) {
                //count++;
                dataCount++;
                // 画面から取得した「WLコントロールマスタ.最大検索件数」件までを表示
                Wbms0510ResultResponse setData = new Wbms0510ResultResponse();
                if (dataCount > searchResultMaxCount) {
                    dataCount--;
                    break;
                }

                setData.setWaterFaucetNo(data.getWaterFaucetNo());                                      // 水栓番号
                setData.setHistoryNo(data.getHistoryNo());                                                // 歴番
                setData.setUserNo(data.getUserNo());                                                      // 使用者番号
                setData.setUserName(data.getUserName());                                                  // 使用者氏名
                setData.setOpenCloseFaucetDiv(data.getOpenCloseFaucetDiv());                          // 開閉栓区分
//                setData.setOpen_close_faucet_div_name(getKbnName(WbmsConstants.WbmsMasKbn.KAIHEI, data.getOpen_close_faucet_div(), sessionInfo.getCorporationCd())); // 開閉栓区分名
                setData.setAdjustmentYm(convertStrYmToStrAlphaWesternCalendar(data.getAdjustmentYm()) );  // 調定年月
                setData.setRegularlyAsNeededDiv(data.getRegularlyAsNeededDiv());            // 定時随時区分
//                setData.setRegularly_as_needed_div_name(getKbnName(WbmsConstants.WbmsMasKbn.TEIZUI, data.getRegularly_as_needed_div(), sessionInfo.getCorporationCd()));            // 定時随時区分名
                setData.setUnreceiptAmount(data.getTwUnreceiptAmount() + data.getSwUnreceiptAmount()); // 未収金額
                setData.setDelinquencyStatusDiv(data.getDelinquencyStatusDiv());                        // 滞納状況名
                setData.setTargetCheck(Constants.STR_ON);                                                 // 選択状態
                setData.setNo(dataCount);                                                                   // 項番
                setData.setFChouteiInfoUpdateDatetime(data.getFChouteiInfoUpdateDatetime());        // 調定情報更新日時
                setData.setAdjustmentNo(data.getAdjustmentNo());                                          // 調定番号

                resData.add(setData);
            }
            // 現在の水栓番号と歴番を直前データ格納用変数に格納
            beforeWaterFaucetNo = nowWaterFaucetNo;
            beforeHistoryNo = nowHistoryNo;

        }
        return resData;
    }

        /**
     * ユニバーサル入力ありの検索処理
     * @param searchResult              検索結果リスト
     * @param unpaidCount           未納件数
     * @param searchResultMaxCount  最大検索結果取得件数
     * @return
     */
    private List<Wbms0510ResultResponse> getSearchUniversal(List<Wbms0510ResultResponse> searchResult, int searchResultMaxCount) {

        // 詰め替え処理
        List<Wbms0510ResultResponse> resData = new ArrayList<Wbms0510ResultResponse>();

        // セッション情報取得
//        List<SessionInfo> sessionInfoList = sessionService.getSessionInfoList();
//        SessionInfo sessionInfo = sessionInfoList.get(0);

        // 返却用変数に検索結果を格納
        // 検索データ総件数カウント用兼レコード採番用変数
        int dataCount = 0;
        // レコードの採番用変数
        //int count = 0;
        for (Wbms0510ResultResponse data : searchResult) {

            //count++;
            dataCount++;
            // 画面から取得した「WLコントロールマスタ.最大検索件数」件までを表示
            Wbms0510ResultResponse setData = new Wbms0510ResultResponse();
            if (dataCount > searchResultMaxCount) {
                dataCount--;
                break;
            }

            setData.setWaterFaucetNo(data.getWaterFaucetNo());                                      // 水栓番号
            setData.setHistoryNo(data.getHistoryNo());                                                // 歴番
            setData.setUserNo(data.getUserNo());                                                      // 使用者番号
            setData.setUserName(data.getUserName());                                                  // 使用者氏名
            setData.setOpenCloseFaucetDiv(data.getOpenCloseFaucetDiv());                          // 開閉栓区分
//            setData.setOpen_close_faucet_div_name(getKbnName(WbmsConstants.WbmsMasKbn.KAIHEI, data.getOpen_close_faucet_div(), sessionInfo.getCorporationCd())); // 開閉栓区分名
            setData.setAdjustmentYm(convertStrYmToStrAlphaWesternCalendar(data.getAdjustmentYm()) );  // 調定年月
            setData.setRegularlyAsNeededDiv(data.getRegularlyAsNeededDiv());            // 定時随時区分
//            setData.setRegularly_as_needed_div_name(getKbnName(WbmsConstants.WbmsMasKbn.TEIZUI, data.getRegularly_as_needed_div(), sessionInfo.getCorporationCd()));            // 定時随時区分名
            setData.setUnreceiptAmount(data.getTwUnreceiptAmount() + data.getSwUnreceiptAmount()); // 未収金額
            setData.setDelinquencyStatusDiv(data.getDelinquencyStatusDiv());                        // 滞納状況名
            setData.setTargetCheck(Constants.STR_ON);                                                 // 選択状態
            setData.setNo(dataCount);                                                                   // 項番

            resData.add(setData);
        }

        return resData;
    }
/***
	 * ログファイルを更新
	 * @param timestamp, wbms0510PrintListRequest
	 * @return
	 */
	private void insertExtractionListLogData(Timestamp timestamp, Wbms0510PrintRequest wbms0510PrintRequest) {

		//Wbms0510ListPrintRequestList listSearch = wbms0510PrintListRequest.getPrintlistConditionDataset();
        Wbms0510PrintConditionRequestDto printCondition = wbms0510PrintRequest.getPrintConditionDto();
        //ijas start
		// List<SessionInfo> sessionInfoList = sessionService.getSessionInfoList();
		// SessionInfo sessionInfo = sessionInfoList.get(0);
		String companyCd = "000001";
        //ijas end
		//Wbms0510PrintDateRequestList conditionDate = wbms0510PrintListRequest.getPrintDateConditionDataset();
        Wbms0510PrintDateRequestDto printScheduleData = wbms0510PrintRequest.getPrintDateDto();

		//Wbms0510ReferenceSearch categoryNameParam = new Wbms0510ReferenceSearch();
        Wbms0510SearchConditionDto printSearchParams = new Wbms0510SearchConditionDto();
		// printSearchParams.setCorporationCd(companyCd);
		// printSearchParams.setKey("92");
		// printSearchParams.setId(printCondition.getId());
		
		//Wbms0510ReferenceTemplateExcel categoryValue = wbms0510Repository.executeCategory(printSearchParams);
		
		String screenId = printCondition.getId();
		String logId = " ";
		String delinquentStatus = " ";
		if (screenId.equals("wbms0511")) { 																	// 督促照会(wbms0511)
			logId = "wbms0501_03";
			delinquentStatus = "4";
		} else if (screenId.equals("wbms0512")) {															    // 催告照会(wbms0512)
			logId = "wbms0503_03";
			delinquentStatus = "6 || 4";
		} else if (screenId.equals("wbms0513")) {															   // 給水停止予告(wbms0513)
			logId = "wbms0504_03";
			delinquentStatus = "8 || 4 || 6";
		} else if (screenId.equals("wbms0514")) { 															// 給水停止執行(wbms0514)
			logId = "wbms0505_03";
			delinquentStatus = "10 || 4 || 6 || 8";
		} else if (screenId.equals("wbms0515")) { 															// 給水停止執行済(wbms0515)
			logId = "wbms0506_03";
			delinquentStatus = "12 || 4 || 6 || 8 || 10";
		}
		// String strSysDATE = DateTimeUtil.convertTimestampToSqlString(timestamp);						// 処理日
		// String strSysTIME = DateTimeUtil.convertTimestampToTimeString(timestamp); 						// 処理時間
		String strSysDATE = null;						// 処理日
		String strSysTIME = null; 						// 処理時間
		// 発行明細を追加するためのエンティティを設定する。
		WbmsFLog insertLogData = new WbmsFLog();
		insertLogData.setCorporationCd(companyCd); 		//ijas								// 事業体コード
		insertLogData.setLogId(logId);																		// 出力ログID
		insertLogData.setOutputDatetime(timestamp);															// 出力日時
		insertLogData.setOperatorId("sessionInfo.getUserCd()");		//ijas											// 操作者ID
		insertLogData.setOperatorName("sessionInfo.getUserNm()");		//ijas											// 操作者名		
		// insertLogData.setOutputConditions("事業体コード =" + companyCd + "調定年月 >=" + 
		// 		listSearch.getAdjustmentYmStart() + "調定年月 <=" + listSearch.getAdjustmentYmEnd() + 
		// 		".滞納状況 =" + delinquentStatus +  "水栓番号 =" + listSearch.getWaterFaucetNo()); 	//ijas			  // 帳票出力条件
		//insertLogData.setFormName(categoryValue.getDivName()); 												// 帳票名
		insertLogData.setWaterFaucetNo(""); 													 // 水栓番号
		insertLogData.setHistoryNo(""); 															 // 歴番
		insertLogData.setRegularlyAsNeededDiv("");									 // 定時随時区分
		insertLogData.setAdjustmentYm(""); 													 // 調定年月
		insertLogData.setIssueNo(""); 																 // 発行番号
		insertLogData.setAdjustmentNo(""); 													 // 調定番号
		insertLogData.setWaterAndSewerageDiv(""); 										 // 上下水区分
		//insertLogData.setIssueDate(conditionDate.getIssue_date()); 											// 発行日付
		insertLogData.setUpdateDate(strSysDATE); 															// 更新日付
		insertLogData.setUpdateTime(strSysTIME); 															// 更新時間
		insertLogData.setUpdatedatetime(timestamp); 														// 更新日時
		insertLogData.setUpdateProgram(WbmsConstants.wbms0510.PROGRAM_ID); 								// 更新プログラム
		insertLogData.setUpdateProgramName(WbmsConstants.wbms0510.PROGRAM_NAME); 							// 更新プログラム名
		insertLogData.setUpdateUserName("sessionInfo.getUserCd()"); 	//ijas											// 更新者名

		// ログファイルINSERT
		wbmsFLogRepository.insert(insertLogData);
	}
    

    /**
     * 検索条件を設定.
     * @param reqSearch Wbms0010SearchCondition
     */
    public Wbms0510DTO setSearchCriteria(Wbms0510RequestList reqSearch, List<ComboItem> paymentDivDataset) {
        // 返却用変数を作成する
    	Wbms0510DTO searchParam = new Wbms0510DTO();

//        // セッション情報取得
//        List<SessionInfo> sessionInfoList = sessionService.getSessionInfoList();
//        SessionInfo sessionInfo = sessionInfoList.get(0);
//
//        // 事業体コード
//        String companyCd = sessionInfo.getCompanyCd();

        // 事業所を設定する
//        searchParam.setCompanyCd(companyCd);

        // 画面IDを設定する。
        if (StringUtil.isNotEmpty(reqSearch.getNextPageID())
                && !Constants.NULL.equals(reqSearch.getNextPageID())) {
            searchParam.setNextPageID(reqSearch.getNextPageID());
        }

        // 調定年月(開始)を設定する。
        if (StringUtil.isNotEmpty(reqSearch.getAdjustmentYmStart())
                && !Constants.NULL.equals(reqSearch.getAdjustmentYmStart())) {
            searchParam.setAdjustmentYmStart(reqSearch.getAdjustmentYmStart());
        }

        // 調定年月(終了)を設定する。
        if (StringUtil.isNotEmpty(reqSearch.getAdjustmentYmEnd())
                && !Constants.NULL.equals(reqSearch.getAdjustmentYmEnd())) {
            searchParam.setAdjustmentYmEnd(reqSearch.getAdjustmentYmEnd());
        }

        // 初回発行・再発行を設定する。
        if (StringUtil.isNotEmpty(reqSearch.getFirstAndRePrintCheck())
                && !Constants.NULL.equals(reqSearch.getFirstAndRePrintCheck())) {
            searchParam.setFirstAndRePrint(reqSearch.getFirstAndRePrintCheck());
        }

        // 滞納状況
        // 画面でチェックされているものを配列に設定する
        if (StringUtil.isNotEmpty(reqSearch.getDelinquencyStatusDiv())
                && !Constants.NULL.equals(reqSearch.getDelinquencyStatusDiv())) {
            searchParam.setDelinquencyStatusDiv(reqSearch.getDelinquencyStatusDiv());
        }

        // ユニバーサル検索を設定する
        if (StringUtil.isNotEmpty(reqSearch.getUniversal())
                && !Constants.NULL.equals(reqSearch.getUniversal())) {

            //カンマ区切りとなっているユニバーサル検索項目を配列に展開
            String[] universal = reqSearch.getUniversal().split(",");

            // List<String>に詰め替える
            List<Wbms0010UniversalParam> universalList = new ArrayList<Wbms0010UniversalParam>();
            for(String s :universal) {
                // 詰め替える時、全角カナを氏名カナ検索用文字列に変換する
                // ひらがなを全角カナに変換する
                String universalKana = StringUtil.hiraganaToZenkakuKana(s);
                // 全角カナを半角ｶﾅに変換する
                universalKana = StringUtil.zenkakuKanaToHankakuKana(universalKana);
                // 半角ｶﾅを検索用文字列に変換する
                universalKana = StringUtil.changeSearchString(universalKana);

                Wbms0010UniversalParam item = new Wbms0010UniversalParam();
                item.setNormal(s.replace("　", "").replace(" ", ""));
                item.setKana(universalKana.replace("　", "").replace(" ", ""));
                universalList.add(item);
            }
            searchParam.setUniversal(universalList);
        } else {

            // 水栓番号を設定する
            searchParam.setWaterFaucetNo(reqSearch.getWaterFaucetNo());

            // 検針地区(開始)を設定する
            if (StringUtil.isNotEmpty(reqSearch.getMeterReadingDistrictStart())
                    && !Constants.NULL.equals(reqSearch.getMeterReadingDistrictStart())) {
                searchParam.setMeterReadingDistrictStart(reqSearch.getMeterReadingDistrictStart());
            }

            // 検針地区(終了)を設定する
            if (StringUtil.isNotEmpty(reqSearch.getMeterReadingDistrictEnd())
                    && !Constants.NULL.equals(reqSearch.getMeterReadingDistrictEnd())) {
                searchParam.setMeterReadingDistrictEnd(reqSearch.getMeterReadingDistrictEnd());
            }

            List<String> paymentList = new ArrayList<String>();
            if (reqSearch.getPaymentDivBankAccount() == "true") {
            	paymentList.add("1");
            }
            if (reqSearch.getPaymentDivReceipt() == "true") {
            	paymentList.add("2");
            }
            if (reqSearch.getPaymentDivCollection() == "true") {
            	paymentList.add("3");
            }
            if (reqSearch.getPaymentDivDelivery() == "true") {
            	paymentList.add("4");
            }

            // 請求区分を設定する。
            // 画面でチェックされているものを配列に設定する
            if (paymentList.size() > 0) {
                searchParam.setPaymentDivCheckedList(paymentList);
            }

            // 未納件数を設定する。
            if (StringUtil.isNotEmpty(reqSearch.getUnreceiptCount())
                    && !Constants.NULL.equals(reqSearch.getUnreceiptCount())) {
                searchParam.setUnpaidCount(reqSearch.getUnreceiptCount());
            }

            // 引抜き情報を設定する。
            if (StringUtil.isNotEmpty(reqSearch.getPullInfoDiv())
                    && !Constants.NULL.equals(reqSearch.getPullInfoDiv())) {
                searchParam.setPullInfoDiv(reqSearch.getPullInfoDiv());
            }
        }
        return searchParam;
    }

    /**
     * Stringのリストを取得する。
     * @param list ComboItemのリスト
     * @return Stringのリスト
     */
    private List<String> getList(List<ComboItem> list) {
        List<String> result = new ArrayList<String>();
        for (ComboItem item: list) {
            result.add(item.getCd());
        }
        return result;
    }

}
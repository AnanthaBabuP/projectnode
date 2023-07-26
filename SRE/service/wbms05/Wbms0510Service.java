package jp.co.wl.service.wbms05;

import org.springframework.stereotype.Service;

import jp.co.wl.request.wbms05.Wbms0510PrintRequest;
import jp.co.wl.request.wbms05.Wbms0510RequestList;
import jp.co.wl.response.wbms05.Wbms0510InitResponse;
import jp.co.wl.response.wbms05.Wbms0510Response;

import net.sf.jasperreports.engine.JasperPrint;
/**
 * 未納照会検索処理のサービス抽象クラス.
 *
 * @author proposal namba
 * @version 1.0.0
 * @since 1.0.0
 */
@Service
public interface Wbms0510Service {

    /**
     * 初期表示処理を実行します。
     * @param request リクエストパラメータ
     * @return 処理結果
     */
    public Wbms0510InitResponse init();

    /**
     * 検索処理を実行します。
     * @param request リクエストパラメータ
     * @return 処理結果
     */
    public Wbms0510Response execute(Wbms0510RequestList Wbms0510RequestList);

     /**
     * 引抜リスト処理を実行します。
     * @param request リクエストパラメータ
     * @return Excelの処理結果
     */
	public JasperPrint executePrintExtractionList(Wbms0510PrintRequest wbms0510PrintRequest);
}

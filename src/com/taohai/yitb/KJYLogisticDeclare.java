package com.taohai.yitb;
//package com.inspireso.declaregz.logistic;
//
//import java.rmi.RemoteException;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.Map;
//import java.util.Set;
//
//import org.datacontract.schemas._2004._07.Uuch_Services.Order;
//import org.datacontract.schemas._2004._07.Uuch_Services.OrderItem;
//import org.datacontract.schemas._2004._07.Uuch_Services.ServiceResult;
//import org.tempuri.IOrder;
//import org.tempuri.IOrderProxy;
//
//import com.inspireso.cross.util.Constant;
//import com.inspireso.declaregz.util.GZConstant;
//import com.inspireso.kuajing.domain.OmsDeclareTask;
//import com.inspireso.kuajing.domain.OmsDeclareTaskItem;
//import com.inspireso.kuajing.domain.Warehouse;
//
///**
// * 跨境易bbc(保税)订单推送
// * @author yangym
// * @version [2014-11-20]
// */
//public class KJYLogisticDeclare implements LogisticStrategy {
//
//	@Override
//	public boolean declare(OmsDeclareTask declareTask, com.inspireso.kuajing.domain.Order order,
//			Map<String, String> ftpParams, String ieFlag, String type, Float weight, Warehouse warehouse) {
//		
//		Date date = declareTask.toDate(declareTask.getCreatedDate());
//		Calendar calender = Calendar.getInstance();
//		calender.setTime(date);
//		
//		Order order1 = new Order(declareTask.getBuyerAccount(), declareTask.getPaymentNo(),
//				order.getCustom(), declareTask.getOrderNumber(),
//				calender, null, getOrderItems(declareTask.getItems()),
//				declareTask.getAmount(), declareTask.getPostFee().toString(),
//				order.getCustomAddress(), order.getCustomCity(),
//				order.getCustomDistrict(), order.getBuyerId(),
//				order.getCustomMobile(), order.getReciverName(),
//				order.getCustomPhone(), order.getCustomProvince(),
//				order.getZip(), order.getEshopName(), Constant.ORDER_STATUS_MAP.get(order.getStatus()),
//				declareTask.getGoodsAmount());
//		IOrder iOrder = new IOrderProxy();
//		try {
//			ServiceResult result = iOrder.addOrder(order1, GZConstant.KJY_USER_NAME,
//					GZConstant.KJY_PASSWORD, GZConstant.KJY_KEY);
//			if(result.getIsSuccess()){
//				declareTask.setCustomMessage("物流订单上传成功");
//			}else{
//				declareTask.setCustomMessage("物流:" + result.getMessage());
//			}
//			declareTask.setExpressStatus(result.getIsSuccess() ? Constant.CUSTOM_STATUS_INDECLARETION
//					: Constant.CUSTOM_STATUS_INVALID);
//			return result.getIsSuccess();
//		} catch (RemoteException e) {
//			declareTask.setCustomMessage("系统异常,请联系管理员!");
//		}
//		return false;
//	}
//
//	/**
//	 * 封装订单详情
//	 * @param declareTaskItems Set<OmsDeclareTaskItem>  
//	 * @return OrderItem[]
//	 */
//	private OrderItem[] getOrderItems(Set<OmsDeclareTaskItem> declareTaskItems){
//		OrderItem[] orderItems = new OrderItem[declareTaskItems.size()];
//		int i = 0;
//		for (OmsDeclareTaskItem declareTaskItem : declareTaskItems) {
//			OrderItem orderItem = new OrderItem(declareTaskItem.getQty().toString(),
//					declareTaskItem.getProductId(), declareTaskItem.getProductId(),
//					"", declareTaskItem.getPrice(),
//					declareTaskItem.getProductId(), declareTaskItem.getGoodsName(),
//					declareTaskItem.getAmount());
//			orderItems[i++] = orderItem;
//		}
//		return orderItems;
//	}
//}

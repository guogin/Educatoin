var wxJSBridgeReadyDeferredObject = $.Deferred();
var wxJSBridgeReadyPromise = wxJSBridgeReadyDeferredObject.promise();

function onBridgeReady() {
    wxJSBridgeReadyDeferredObject.resolve();
}

if (typeof WeixinJSBridge == "undefined") {
   if( document.addEventListener ){
       document.addEventListener('WeixinJSBridgeReady', onBridgeReady, false);
   }else if (document.attachEvent){
       document.attachEvent('WeixinJSBridgeReady', onBridgeReady);
       document.attachEvent('onWeixinJSBridgeReady', onBridgeReady);
   }
} else {
   onBridgeReady();
}

function initiatePay(request) {
    wxJSBridgeReadyPromise.done(function() {
        WeixinJSBridge.invoke(
            'getBrandWCPayRequest', {
                "appId":        request.appId,           //公众号名称，由商户传入
                "timeStamp":    request.timeStamp,       //时间戳，自1970年以来的秒数
                "nonceStr":     request.nonceStr,        //随机串
                "package":      request.package,
                "signType":     request.signType,        //微信签名方式
                "paySign":      request.paySign          //微信签名
            },
            function(res) {
                // 使用以上方式判断前端返回,微信团队郑重提示：res.err_msg将在用户支付成功后返回
                // ok，但并不保证它绝对可靠。
                if(res.err_msg == "get_brand_wcpay_request:ok" ) {
                    // TODO: navigate to the order page or success paid info page
                    // TODO: But please make sure the code above works as document first
                }
            }
        );
    });
}

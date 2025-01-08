import router from '../router'
//let a = {name: hello , age : 10}
//qs.Stringfy(a)
//
import Qs from 'qs';

//包装了ajax ，方便http调用
import axios from 'axios';

//通用公共方法(包含回调)
/**
 *
 * @param method 'GET'、'POST'、'PUT'、'DELETE'
 * @param baseUrl 请求的基础 URL。通常是服务器的根路径。例如：https://api.example.com 与 url 组合形成完整的请求地址
 * @param url 请求的具体路径 '/users'，如果 baseUrl 是 https://api.example.com，那么完整的 URL 就是 https://api.example.com/users
 * @param params 请求参数，包含要发送给服务器的数据
 * @param callback 回调函数，处理服务器响应的数据。这个回调函数在请求成功时被调用，接收三个参数：code、message 和 data
 * @returns {*}
 */
export const reqRealEndAsync = (method, baseUrl,
                                url, params, callback) => {
    params.token = sessionStorage.getItem('token');
    return axios({
        timeout: 5000,
        baseURL: baseUrl,
        method: method,
        url: url,
        headers: {
            'Content-type': 'application/x-www-form-urlencoded',
        },
        data: Qs.stringify(params),
        /*
        * TODO
        *  因为 let a = {name: hello , age : 10}
        *  执行了qs.Stringfy(a)  变成了name=hello&age=13 这个方柏霓在url上面展示
        *  但是传给后端的话 还是用数组{name: hello , age : 10}
        *  所以用 true ☆☆☆☆☆☆☆☆☆☆☆☆
        *  false的话 需要用java的split
        * */
        traditional: true,
    }).then(res => {
        let result = res.data;
        /**
         * TODO
         *  前后端的数据格式 {code : 0 -- 成功的 1/2/其他 -- 失败,message: , data:{}}
         */
        if (result.code == 1) {
            //验证失败
            router.replace({
                path: "login",
                query: {
                    msg: result.message
                }
            });
        } else if (result.code == 0) {
            //成功回调
            if (callback != undefined) {
                callback(result.code, result.message, result.data);
            }
        }else if (result.code == 2) {
            //成功回调
            if (callback != undefined) {
                callback(result.code, result.message, result.data);
            }
        }
    });
};

//通用公共方法(不包含回调)
export const reqRealEnd = (method, baseUrl,
                           url, params) => {
    params.token = sessionStorage.getItem('token');
    return axios({
        timeout: 5000,
        baseURL: baseUrl,
        method: method,
        url: url,
        headers:{
            'Content-type': 'application/x-www-form-urlencoded',
        },
        data: Qs.stringify(params),

        traditional: true,
    });
};

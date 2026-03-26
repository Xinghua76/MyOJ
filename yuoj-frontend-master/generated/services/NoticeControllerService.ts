import type { CancelablePromise } from "../core/CancelablePromise";
import { OpenAPI } from "../core/OpenAPI";
import { request as __request } from "../core/request";

export class NoticeControllerService {
  public static addUsingPost(request: any): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: "POST",
      url: "/api/notice/add",
      body: request,
    });
  }
  public static deleteUsingPost(request: any): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: "POST",
      url: "/api/notice/delete",
      body: request,
    });
  }
  public static updateUsingPost(request: any): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: "POST",
      url: "/api/notice/update",
      body: request,
    });
  }
  public static listPageUsingPost(request: any): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: "POST",
      url: "/api/notice/list/page/vo",
      body: request,
    });
  }
}

import type { CancelablePromise } from "../core/CancelablePromise";
import { OpenAPI } from "../core/OpenAPI";
import { request as __request } from "../core/request";

export class AdminOpLogControllerService {
  public static listAdminOpLogByPageUsingPost(
    pageRequest: any
  ): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: "POST",
      url: "/api/admin_op_log/list/page",
      body: pageRequest,
      errors: {
        401: `Unauthorized`,
        403: `Forbidden`,
        404: `Not Found`,
      },
    });
  }
}

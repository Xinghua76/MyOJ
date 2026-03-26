/* generated using openapi-typescript-codegen -- do no edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { ContestQuestionVO } from '../models/ContestQuestionVO';
import type { ContestQuestionQueryRequest } from '../models/ContestQuestionQueryRequest';
import type { ContestQuestionAddRequest } from '../models/ContestQuestionAddRequest';
import type { DeleteRequest } from '../models/DeleteRequest';
import type { BaseResponse_Page_ContestQuestionVO_ } from '../models/BaseResponse_Page_ContestQuestionVO_';
import type { BaseResponse_long_ } from '../models/BaseResponse_long_';
import type { BaseResponse_boolean_ } from '../models/BaseResponse_boolean_';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';

export class ContestQuestionControllerService {

    /**
     * addContestQuestion
     * @param contestQuestionAddRequest contestQuestionAddRequest
     * @returns BaseResponse_long_ OK
     * @throws ApiError
     */
    public static addContestQuestionUsingPost(
        contestQuestionAddRequest: ContestQuestionAddRequest,
    ): CancelablePromise<BaseResponse_long_> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/contest_question/add',
            body: contestQuestionAddRequest,
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
                404: `Not Found`,
            },
        });
    }

    /**
     * deleteContestQuestion
     * @param deleteRequest deleteRequest
     * @returns BaseResponse_boolean_ OK
     * @throws ApiError
     */
    public static deleteContestQuestionUsingPost(
        deleteRequest: DeleteRequest,
    ): CancelablePromise<BaseResponse_boolean_> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/contest_question/delete',
            body: deleteRequest,
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
                404: `Not Found`,
            },
        });
    }

    /**
     * listContestQuestionByPage
     * @param contestQuestionQueryRequest contestQuestionQueryRequest
     * @returns BaseResponse_Page_ContestQuestionVO_ OK
     * @throws ApiError
     */
    public static listContestQuestionByPageUsingPost(
        contestQuestionQueryRequest: ContestQuestionQueryRequest,
    ): CancelablePromise<BaseResponse_Page_ContestQuestionVO_> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/contest_question/list/page',
            body: contestQuestionQueryRequest,
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
                404: `Not Found`,
            },
        });
    }

}

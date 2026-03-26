/* generated using openapi-typescript-codegen -- do no edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { ContestRankVO } from '../models/ContestRankVO';
import type { ContestRankQueryRequest } from '../models/ContestRankQueryRequest';
import type { BaseResponse_Page_ContestRankVO_ } from '../models/BaseResponse_Page_ContestRankVO_';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';

export class ContestRankControllerService {

    /**
     * listContestRankByPage
     * @param contestRankQueryRequest contestRankQueryRequest
     * @returns BaseResponse_Page_ContestRankVO_ OK
     * @throws ApiError
     */
    public static listContestRankByPageUsingPost(
        contestRankQueryRequest: ContestRankQueryRequest,
    ): CancelablePromise<BaseResponse_Page_ContestRankVO_> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/contest_rank/list/page',
            body: contestRankQueryRequest,
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
                404: `Not Found`,
            },
        });
    }

}

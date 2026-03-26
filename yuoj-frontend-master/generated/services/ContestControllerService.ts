/* generated using openapi-typescript-codegen -- do no edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { ContestVO } from '../models/ContestVO';
import type { BaseResponse_boolean_ } from '../models/BaseResponse_boolean_';
import type { ContestQueryRequest } from '../models/ContestQueryRequest';
import type { BaseResponse_Page_ContestVO_ } from '../models/BaseResponse_Page_ContestVO_';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';

export class ContestControllerService {

    /**
     * listContestVOByPage
     * @param contestQueryRequest contestQueryRequest
     * @returns BaseResponse_Page_ContestVO_ OK
     * @throws ApiError
     */
    public static listContestVoByPageUsingPost(
        contestQueryRequest: ContestQueryRequest,
    ): CancelablePromise<BaseResponse_Page_ContestVO_> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/contest/list/page/vo',
            body: contestQueryRequest,
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
                404: `Not Found`,
            },
        });
    }

    /**
     * listContestByPage
     * @param contestQueryRequest contestQueryRequest
     * @returns BaseResponse_Page_Contest_ OK
     * @throws ApiError
     */
    public static listContestByPageUsingPost(
        contestQueryRequest: ContestQueryRequest,
    ): CancelablePromise<any> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/contest/list/page',
            body: contestQueryRequest,
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
                404: `Not Found`,
            },
        });
    }

    /**
     * add
     * @param request request
     * @returns BaseResponse_long_ OK
     * @throws ApiError
     */
    public static addUsingPost(
        request: any,
    ): CancelablePromise<any> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/contest/add',
            body: request,
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
                404: `Not Found`,
            },
        });
    }

    /**
     * update
     * @param request request
     * @returns BaseResponse_boolean_ OK
     * @throws ApiError
     */
    public static updateUsingPost(
        request: any,
    ): CancelablePromise<any> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/contest/update',
            body: request,
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
                404: `Not Found`,
            },
        });
    }

    /**
     * delete
     * @param request request
     * @returns BaseResponse_boolean_ OK
     * @throws ApiError
     */
    public static deleteUsingPost(
        request: any,
    ): CancelablePromise<any> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/contest/delete',
            body: request,
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
                404: `Not Found`,
            },
        });
    }

    /**
     * getContestVOById
     * @param id id
     * @returns BaseResponse_ContestVO_ OK
     * @throws ApiError
     */
    public static getContestVoByIdUsingGet(
        id: number | string,
    ): CancelablePromise<BaseResponse_Page_ContestVO_> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/contest/get/vo',
            query: {
                'id': id,
            },
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
                404: `Not Found`,
            },
        });
    }

}

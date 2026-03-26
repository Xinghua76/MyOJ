/* generated using openapi-typescript-codegen -- do no edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { BaseResponse_Page_SolutionCommentVO_ } from '../models/BaseResponse_Page_SolutionCommentVO_';
import type { DeleteRequest } from '../models/DeleteRequest';
import type { SolutionCommentAddRequest } from '../models/SolutionCommentAddRequest';
import type { SolutionCommentQueryRequest } from '../models/SolutionCommentQueryRequest';
import type { SolutionCommentUpdateRequest } from '../models/SolutionCommentUpdateRequest';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';

export class SolutionCommentControllerService {

    /**
     * add
     * @param solutionCommentAddRequest solutionCommentAddRequest
     * @returns BaseResponse_long_ OK
     * @returns any Created
     * @throws ApiError
     */
    public static addUsingPost(
        solutionCommentAddRequest: SolutionCommentAddRequest,
    ): CancelablePromise<any> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/solution_comment/add',
            body: solutionCommentAddRequest,
        });
    }

    /**
     * delete
     * @param deleteRequest deleteRequest
     * @returns BaseResponse_boolean_ OK
     * @returns any Created
     * @throws ApiError
     */
    public static deleteUsingPost(
        deleteRequest: DeleteRequest,
    ): CancelablePromise<any> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/solution_comment/delete',
            body: deleteRequest,
        });
    }

    /**
     * listMyPage
     * @param solutionCommentQueryRequest solutionCommentQueryRequest
     * @returns BaseResponse_Page_SolutionCommentVO_ OK
     * @returns any Created
     * @throws ApiError
     */
    public static listMyPageUsingPost(
        solutionCommentQueryRequest: SolutionCommentQueryRequest,
    ): CancelablePromise<BaseResponse_Page_SolutionCommentVO_> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/solution_comment/my/list/page/vo',
            body: solutionCommentQueryRequest,
        });
    }

    /**
     * listPage
     * @param solutionCommentQueryRequest solutionCommentQueryRequest
     * @returns BaseResponse_Page_SolutionCommentVO_ OK
     * @returns any Created
     * @throws ApiError
     */
    public static listPageUsingPost(
        solutionCommentQueryRequest: SolutionCommentQueryRequest,
    ): CancelablePromise<BaseResponse_Page_SolutionCommentVO_> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/solution_comment/list/page/vo',
            body: solutionCommentQueryRequest,
        });
    }

    /**
     * update
     * @param solutionCommentUpdateRequest solutionCommentUpdateRequest
     * @returns BaseResponse_boolean_ OK
     * @returns any Created
     * @throws ApiError
     */
    public static updateUsingPost(
        solutionCommentUpdateRequest: SolutionCommentUpdateRequest,
    ): CancelablePromise<any> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/solution_comment/update',
            body: solutionCommentUpdateRequest,
        });
    }

}
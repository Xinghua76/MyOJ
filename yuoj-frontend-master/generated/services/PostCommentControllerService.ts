/* generated using openapi-typescript-codegen -- do no edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { BaseResponse_Page_PostCommentVO_ } from '../models/BaseResponse_Page_PostCommentVO_';
import type { DeleteRequest } from '../models/DeleteRequest';
import type { PostCommentAddRequest } from '../models/PostCommentAddRequest';
import type { PostCommentQueryRequest } from '../models/PostCommentQueryRequest';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';

export class PostCommentControllerService {

    /**
     * add
     * @param postCommentAddRequest postCommentAddRequest
     * @returns BaseResponse_long_ OK
     * @returns any Created
     * @throws ApiError
     */
    public static addPostCommentUsingPost(
        postCommentAddRequest: PostCommentAddRequest,
    ): CancelablePromise<any> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/post_comment/add',
            body: postCommentAddRequest,
        });
    }

    /**
     * delete
     * @param deleteRequest deleteRequest
     * @returns BaseResponse_boolean_ OK
     * @returns any Created
     * @throws ApiError
     */
    public static deletePostCommentUsingPost(
        deleteRequest: DeleteRequest,
    ): CancelablePromise<any> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/post_comment/delete',
            body: deleteRequest,
        });
    }

    /**
     * listPage
     * @param postCommentQueryRequest postCommentQueryRequest
     * @returns BaseResponse_Page_PostCommentVO_ OK
     * @returns any Created
     * @throws ApiError
     */
    public static listPostCommentVoByPageUsingPost(
        postCommentQueryRequest: PostCommentQueryRequest,
    ): CancelablePromise<BaseResponse_Page_PostCommentVO_> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/post_comment/list/page/vo',
            body: postCommentQueryRequest,
        });
    }

    /**
     * doThumb
     * @param deleteRequest deleteRequest
     * @returns BaseResponse_integer_ OK
     * @returns any Created
     * @throws ApiError
     */
    public static doPostCommentThumbUsingPost(
        deleteRequest: DeleteRequest,
    ): CancelablePromise<any> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/post_comment/do_thumb',
            body: deleteRequest,
        });
    }

}
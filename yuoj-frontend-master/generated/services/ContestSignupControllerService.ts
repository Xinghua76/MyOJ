/* generated using openapi-typescript-codegen -- do no edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { ContestSignup } from '../models/ContestSignup';
import type { ContestSignupQueryRequest } from '../models/ContestSignupQueryRequest';
import type { ContestSignupRequest } from '../models/ContestSignupRequest';
import type { DeleteRequest } from '../models/DeleteRequest';
import type { BaseResponse_Page_ContestSignup_ } from '../models/BaseResponse_Page_ContestSignup_';
import type { BaseResponse_boolean_ } from '../models/BaseResponse_boolean_';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';

export class ContestSignupControllerService {

    /**
     * cancelContestSignup
     * @param deleteRequest deleteRequest
     * @returns BaseResponse_boolean_ OK
     * @throws ApiError
     */
    public static cancelContestSignupUsingPost(
        deleteRequest: DeleteRequest,
    ): CancelablePromise<BaseResponse_boolean_> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/contest_signup/cancel',
            body: deleteRequest,
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
                404: `Not Found`,
            },
        });
    }

    /**
     * listContestSignupByPage
     * @param contestSignupQueryRequest contestSignupQueryRequest
     * @returns BaseResponse_Page_ContestSignup_ OK
     * @throws ApiError
     */
    public static listContestSignupByPageUsingPost(
        contestSignupQueryRequest: ContestSignupQueryRequest,
    ): CancelablePromise<BaseResponse_Page_ContestSignup_> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/contest_signup/list/page',
            body: contestSignupQueryRequest,
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
                404: `Not Found`,
            },
        });
    }

    /**
     * signup
     * @param contestSignupRequest contestSignupRequest
     * @returns BaseResponse_boolean_ OK
     * @throws ApiError
     */
    public static signupUsingPost(
        contestSignupRequest: ContestSignupRequest,
    ): CancelablePromise<BaseResponse_boolean_> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/contest_signup/do',
            body: contestSignupRequest,
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
                404: `Not Found`,
            },
        });
    }

}

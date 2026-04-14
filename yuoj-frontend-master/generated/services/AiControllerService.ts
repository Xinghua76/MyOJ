/* generated using openapi-typescript-codegen -- do no edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { AiAnalysisRequest } from '../models/AiAnalysisRequest';
import type { BaseResponse_string_ } from '../models/BaseResponse_string_';

import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';

export class AiControllerService {

    /**
     * analyzeCode
     * @param aiAnalysisRequest aiAnalysisRequest
     * @returns BaseResponse_string_ OK
     * @throws ApiError
     */
    public static analyzeCodeUsingPost(
        aiAnalysisRequest: AiAnalysisRequest,
    ): CancelablePromise<BaseResponse_string_> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/ai/analyze',
            body: aiAnalysisRequest,
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
                404: `Not Found`,
            },
        });
    }

}

import { request, buildQuery } from './api.js';

export const predictionService = {
    getPricePrediction: (areaSqMt, lid, nights) =>
        request(`/predictions/price?${buildQuery({ areaSqMt, lid, nights })}`, 'GET')
};

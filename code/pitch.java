public void run() {
    while (true) {
        // Read audio data into audioBuffer
        audioRecord.read(audioBuffer, 0, audioBuffer.length);
        
        // Perform autocorrelation for each segment of audioBuffer
        for (int ii = 0; ii < 3; ii++) {
            // Prepare data for FFT
            for (int k = 0; k < N; k++)
                x[k].set(audioBuffer[audioBuffer.length / 3 * ii + k], 0);

            // Perform FFT
            X = fft(x);

            // Calculate power spectrum
            for (int k = 0; k < X.length; k++)
                X[k] = X[k].times(X[k].conjugate());

            // Perform IFFT
            autocorrC = ifft(X);

            // Extract real part of IFFT result
            for (int k = 0; k < N; k++)
                autocorr[k] = autocorrC[k].re();

            // Find the index of the maximum autocorrelation value
            int min0 = -1;
            int minL = -1;
            double value01 = 0;
            int max1 = -1;
            for (int i = 0; i < N; i++)
                if (autocorr[i] < 0) {
                    min0 = i;
                    break;
                }
            for (int i = N - 1; i >= 0; i--)
                if (autocorr[i] < 0) {
                    minL = i;
                    break;
                }
            for (int i = 0; i < minL; i++) {
                if (i < min0)
                    continue;
                if (autocorr[i] > value01) {
                    max1 = i;
                    value01 = autocorr[i];
                }
            }

            // Calculate frequency from index of maximum autocorrelation value
            freq[ii] = FS / (float) max1;

            // Calculate decibel level of the audio segment
            double p2 = 0;
            for (int i = 0; i < audioBuffer.length; i++)
                p2 += audioBuffer[i];
            p2 /= audioBuffer.length;
            if (p2 == 0)
                decibel[ii] = Double.NEGATIVE_INFINITY;
            else
                decibel[ii] = 20.0 * Math.log10(p2 / 65535.0);
        }
        counter++; // Increment counter
    }
}
